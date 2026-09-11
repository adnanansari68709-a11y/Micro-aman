package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MicroAmanApplication
import com.example.model.AutomationLog
import com.example.model.Macro
import com.example.model.MacroAction
import com.example.model.MacroConstraint
import com.example.model.MacroTrigger
import com.example.model.Variable
import com.example.service.AutomationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MicroAmanApplication
    private val repository = app.repository
    private val engine = app.automationEngine

    val isMasterEnabled: StateFlow<Boolean> = engine.isMasterEnabled

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _logFilter = MutableStateFlow("ALL")
    val logFilter: StateFlow<String> = _logFilter.asStateFlow()

    // Base flows
    val allMacros: StateFlow<List<Macro>> = repository.allMacros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMacros: StateFlow<List<Macro>> = combine(allMacros, _searchQuery, _categoryFilter) { list, query, category ->
        list.filter { macro ->
            val matchesCategory = (category == "All" || macro.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    macro.name.contains(query, ignoreCase = true) ||
                    macro.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<AutomationLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredLogs: StateFlow<List<AutomationLog>> = combine(recentLogs, _logFilter) { logs, filter ->
        if (filter == "ALL") logs else logs.filter { it.eventType.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVariables: StateFlow<List<Variable>> = repository.allVariables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.deduplicateMacros()
        }
    }

    // Macro Builder Draft State
    private val _isBuilderOpen = MutableStateFlow(false)
    val isBuilderOpen: StateFlow<Boolean> = _isBuilderOpen.asStateFlow()

    private val _builderDraft = MutableStateFlow<Macro?>(null)
    val builderDraft: StateFlow<Macro?> = _builderDraft.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun setLogFilter(filter: String) {
        _logFilter.value = filter
    }

    fun toggleMasterEngine(enabled: Boolean) {
        engine.setMasterEnabled(enabled)
        if (enabled) {
            AutomationService.start(app)
        } else {
            AutomationService.stop(app)
        }
        viewModelScope.launch {
            _snackbarEvent.emit(if (enabled) "Micro Aman automation engine is ON" else "Micro Aman engine paused")
        }
    }

    fun toggleMacro(macroId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.setMacroEnabled(macroId, enabled)
            _snackbarEvent.emit(if (enabled) "Macro enabled" else "Macro disabled")
        }
    }

    fun deleteMacro(macroId: String) {
        viewModelScope.launch {
            repository.deleteMacro(macroId)
            _snackbarEvent.emit("Macro deleted")
        }
    }

    fun testMacro(macro: Macro) {
        engine.testMacro(macro)
        viewModelScope.launch {
            _snackbarEvent.emit("Testing actions for '${macro.name}'...")
        }
    }

    fun importTemplate(template: Macro) {
        viewModelScope.launch {
            val exists = allMacros.value.any {
                it.name.equals(template.name, ignoreCase = true) ||
                it.name.startsWith(template.name, ignoreCase = true)
            }
            if (exists) {
                _snackbarEvent.emit("'${template.name}' is already in your Macros!")
                _selectedTab.value = 0
                return@launch
            }
            val copy = template.copy(
                id = UUID.randomUUID().toString(),
                name = template.name,
                enabled = true,
                triggerCount = 0,
                lastTriggeredAt = 0L
            )
            repository.saveMacro(copy)
            _snackbarEvent.emit("Added '${copy.name}' to My Macros")
            _selectedTab.value = 0
        }
    }

    // Builder Functions
    fun openNewMacroBuilder() {
        _builderDraft.value = Macro(
            id = UUID.randomUUID().toString(),
            name = "",
            description = "",
            category = "Custom",
            enabled = true,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )
        _isBuilderOpen.value = true
    }

    fun openEditMacroBuilder(macro: Macro) {
        _builderDraft.value = macro
        _isBuilderOpen.value = true
    }

    fun closeBuilder() {
        _isBuilderOpen.value = false
        _builderDraft.value = null
    }

    fun updateDraftMetadata(name: String, description: String, category: String, colorTag: String) {
        _builderDraft.value = _builderDraft.value?.copy(
            name = name,
            description = description,
            category = category,
            colorTag = colorTag
        )
    }

    fun addTriggerToDraft(trigger: MacroTrigger) {
        val current = _builderDraft.value ?: return
        _builderDraft.value = current.copy(triggers = current.triggers + trigger)
    }

    fun removeTriggerFromDraft(triggerId: String) {
        val current = _builderDraft.value ?: return
        _builderDraft.value = current.copy(triggers = current.triggers.filterNot { it.id == triggerId })
    }

    fun addActionToDraft(action: MacroAction) {
        val current = _builderDraft.value ?: return
        _builderDraft.value = current.copy(actions = current.actions + action)
    }

    fun removeActionFromDraft(actionId: String) {
        val current = _builderDraft.value ?: return
        _builderDraft.value = current.copy(actions = current.actions.filterNot { it.id == actionId })
    }

    fun addConstraintToDraft(constraint: MacroConstraint) {
        val current = _builderDraft.value ?: return
        _builderDraft.value = current.copy(constraints = current.constraints + constraint)
    }

    fun removeConstraintFromDraft(constraintId: String) {
        val current = _builderDraft.value ?: return
        _builderDraft.value = current.copy(constraints = current.constraints.filterNot { it.id == constraintId })
    }

    fun saveDraftMacro() {
        val draft = _builderDraft.value ?: return
        if (draft.name.isBlank()) {
            viewModelScope.launch { _snackbarEvent.emit("Please enter a macro name") }
            return
        }
        if (draft.triggers.isEmpty()) {
            viewModelScope.launch { _snackbarEvent.emit("Please add at least one trigger") }
            return
        }
        if (draft.actions.isEmpty()) {
            viewModelScope.launch { _snackbarEvent.emit("Please add at least one action") }
            return
        }

        viewModelScope.launch {
            repository.saveMacro(draft)
            _isBuilderOpen.value = false
            _builderDraft.value = null
            _snackbarEvent.emit("Macro '${draft.name}' saved successfully!")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _snackbarEvent.emit("System logs cleared")
        }
    }

    fun saveVariable(name: String, type: String, value: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.saveVariable(Variable(name.trim(), type, value))
            _snackbarEvent.emit("Variable '$name' saved")
        }
    }

    fun deleteVariable(name: String) {
        viewModelScope.launch {
            repository.deleteVariable(name)
            _snackbarEvent.emit("Variable '$name' deleted")
        }
    }

    fun testSpeech(text: String) {
        engine.speakOut(text)
    }

    fun testVibration() {
        engine.vibrateDevice("DOUBLE_PULSE")
    }

    suspend fun exportJson(): String {
        return repository.exportAllMacrosJson()
    }

    fun importJson(jsonStr: String) {
        viewModelScope.launch {
            try {
                val count = repository.importMacrosFromJson(jsonStr)
                _snackbarEvent.emit("Successfully imported $count macros!")
            } catch (e: Exception) {
                _snackbarEvent.emit("Import failed: Invalid JSON")
            }
        }
    }
}
