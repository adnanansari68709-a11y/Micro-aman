package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.service.AutomationService
import com.example.ui.MainViewModel
import com.example.ui.components.MacroBuilderDialog
import com.example.ui.components.TopAppBarWithMasterSwitch
import com.example.ui.screens.MacrosListScreen
import com.example.ui.screens.SystemLogScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.screens.VariablesAndSettingsScreen
import com.example.ui.theme.AmanCyan
import com.example.ui.theme.AmanDarkBg
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.MicroAmanTheme
import kotlinx.coroutines.flow.collectLatest

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start background automation engine service
        AutomationService.start(this)

        setContent {
            MicroAmanTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isMasterEnabled by viewModel.isMasterEnabled.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.categoryFilter.collectAsState()
    val logFilter by viewModel.logFilter.collectAsState()

    val macros by viewModel.filteredMacros.collectAsState()
    val allMacros by viewModel.allMacros.collectAsState()
    val logs by viewModel.filteredLogs.collectAsState()
    val variables by viewModel.allVariables.collectAsState()

    val isBuilderOpen by viewModel.isBuilderOpen.collectAsState()
    val builderDraft by viewModel.builderDraft.collectAsState()

    // Request notification permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission handled gracefully */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Collect snackbar events
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AmanDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithMasterSwitch(
                isMasterEnabled = isMasterEnabled,
                onToggleMaster = { viewModel.toggleMasterEngine(it) },
                macroCount = allMacros.size
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AmanDarkSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .testTag("bottom_nav_bar"),
                tonalElevation = 8.dp
            ) {
                // 1. Macros Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Macros") },
                    label = { Text("Macros") },
                    modifier = Modifier.testTag("nav_tab_macros"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmanCyan,
                        selectedTextColor = AmanCyan,
                        indicatorColor = AmanCyan.copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                // 2. Templates Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.Widgets, contentDescription = "Templates") },
                    label = { Text("Templates") },
                    modifier = Modifier.testTag("nav_tab_templates"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmanCyan,
                        selectedTextColor = AmanCyan,
                        indicatorColor = AmanCyan.copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                // 3. System Log Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Logs") },
                    label = { Text("Log") },
                    modifier = Modifier.testTag("nav_tab_logs"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmanCyan,
                        selectedTextColor = AmanCyan,
                        indicatorColor = AmanCyan.copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                // 4. Variables & Settings Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_tab_settings"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmanCyan,
                        selectedTextColor = AmanCyan,
                        indicatorColor = AmanCyan.copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> MacrosListScreen(
                    macros = macros,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.setCategoryFilter(it) },
                    onToggleMacro = { id, enabled -> viewModel.toggleMacro(id, enabled) },
                    onTestMacro = { viewModel.testMacro(it) },
                    onEditMacro = { viewModel.openEditMacroBuilder(it) },
                    onDeleteMacro = { viewModel.deleteMacro(it) },
                    onAddNewMacro = { viewModel.openNewMacroBuilder() }
                )
                1 -> TemplatesScreen(
                    onImportTemplate = { viewModel.importTemplate(it) },
                    existingMacros = macros
                )
                2 -> SystemLogScreen(
                    logs = logs,
                    selectedFilter = logFilter,
                    onFilterSelect = { viewModel.setLogFilter(it) },
                    onClearLogs = { viewModel.clearLogs() }
                )
                3 -> VariablesAndSettingsScreen(
                    variables = variables,
                    onSaveVariable = { name, type, value -> viewModel.saveVariable(name, type, value) },
                    onDeleteVariable = { viewModel.deleteVariable(it) },
                    onTestSpeech = { viewModel.testSpeech(it) },
                    onTestVibration = { viewModel.testVibration() },
                    onExportJson = { viewModel.exportJson() },
                    onImportJson = { viewModel.importJson(it) }
                )
            }
        }
    }

    // Macro Builder Dialog
    if (isBuilderOpen && builderDraft != null) {
        MacroBuilderDialog(
            macroDraft = builderDraft!!,
            onUpdateMetadata = { name, desc, cat, colorTag ->
                viewModel.updateDraftMetadata(name, desc, cat, colorTag)
            },
            onAddTrigger = { viewModel.addTriggerToDraft(it) },
            onRemoveTrigger = { viewModel.removeTriggerFromDraft(it) },
            onAddAction = { viewModel.addActionToDraft(it) },
            onRemoveAction = { viewModel.removeActionFromDraft(it) },
            onAddConstraint = { viewModel.addConstraintToDraft(it) },
            onRemoveConstraint = { viewModel.removeConstraintFromDraft(it) },
            onTestMacro = { viewModel.testMacro(it) },
            onSave = { viewModel.saveDraftMacro() },
            onDismiss = { viewModel.closeBuilder() }
        )
    }
}
