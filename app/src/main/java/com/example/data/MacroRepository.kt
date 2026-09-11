package com.example.data

import com.example.model.ActionType
import com.example.model.AutomationLog
import com.example.model.ConstraintType
import com.example.model.Macro
import com.example.model.MacroAction
import com.example.model.MacroConstraint
import com.example.model.MacroTrigger
import com.example.model.TriggerType
import com.example.model.Variable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MacroRepository(
    private val macroDao: MacroDao,
    private val logDao: LogDao,
    private val variableDao: VariableDao
) {
    private val initMutex = Mutex()
    val allMacros: Flow<List<Macro>> = macroDao.getAllMacros().map { entities ->
        entities.map { it.toMacro() }
    }

    val recentLogs: Flow<List<AutomationLog>> = logDao.getRecentLogs().map { entities ->
        entities.map { it.toLog() }
    }

    val allVariables: Flow<List<Variable>> = variableDao.getAllVariables().map { entities ->
        entities.map { it.toVariable() }
    }

    suspend fun getEnabledMacros(): List<Macro> {
        return macroDao.getEnabledMacrosOnce().map { it.toMacro() }
    }

    suspend fun saveMacro(macro: Macro) {
        macroDao.insertMacro(MacroEntity.fromMacro(macro))
    }

    suspend fun setMacroEnabled(id: String, enabled: Boolean) {
        macroDao.setMacroEnabled(id, enabled)
    }

    suspend fun recordTrigger(id: String) {
        macroDao.incrementTriggerCount(id, System.currentTimeMillis())
    }

    suspend fun deleteMacro(id: String) {
        macroDao.deleteMacroById(id)
    }

    suspend fun logEvent(macroName: String, eventType: String, detail: String, status: String) {
        logDao.insertLog(
            LogEntity(
                timestamp = System.currentTimeMillis(),
                macroName = macroName,
                eventType = eventType,
                detail = detail,
                status = status
            )
        )
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }

    suspend fun getAllLogsOnce(): List<AutomationLog> {
        return logDao.getAllLogsOnce().map { it.toLog() }
    }

    suspend fun saveVariable(variable: Variable) {
        variableDao.insertVariable(VariableEntity.fromVariable(variable))
    }

    suspend fun deleteVariable(name: String) {
        variableDao.deleteVariable(name)
    }

    // Export all macros to a formatted JSON string
    suspend fun exportAllMacrosJson(): String {
        val macros = macroDao.getEnabledMacrosOnce().map { it.toMacro() }
        val array = JSONArray()
        macros.forEach { array.put(JSONObject(it.toJson())) }
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "Micro Aman")
        root.put("macros", array)
        return root.toString(2)
    }

    // Import macros from JSON
    suspend fun importMacrosFromJson(jsonStr: String): Int {
        val root = JSONObject(jsonStr)
        val array = root.optJSONArray("macros") ?: JSONArray()
        var count = 0
        for (i in 0 until array.length()) {
            val macroObj = array.getJSONObject(i)
            val macro = Macro.fromJson(macroObj.toString())
            saveMacro(macro)
            count++
        }
        return count
    }

    // Deduplicate any macros in database that share the same name or duplicate features
    suspend fun deduplicateMacros() {
        val all = macroDao.getAllMacrosOnce()
        // 1. Remove legacy duplicate IDs to ensure single clean feature
        for (m in all) {
            if (m.id == "default_charger_connected" && all.any { it.id == "default_charging_silent" }) {
                macroDao.deleteMacroById(m.id)
            }
            if (m.id == "default_charger_unplugged" && all.any { it.id == "default_unplug_sound" }) {
                macroDao.deleteMacroById(m.id)
            }
        }

        // 2. Deduplicate by normalized name
        val remaining = macroDao.getAllMacrosOnce()
        val seenNames = mutableSetOf<String>()
        for (m in remaining) {
            val normalizedName = m.name.trim().lowercase()
            if (seenNames.contains(normalizedName)) {
                macroDao.deleteMacroById(m.id)
            } else {
                seenNames.add(normalizedName)
            }
        }
    }

    // Pre-loaded popular MacroDroid automations with Certified Hindi Voices
    suspend fun populateDefaultTemplatesIfEmpty() {
        initMutex.withLock {
            // Clean up any duplicate records first
            deduplicateMacros()

            if (macroDao.getMacroCount() > 0) return

            val templates = listOf(
                Macro(
                    id = "default_battery_100",
                    name = "Battery Full 100% Voice Alarm",
                    description = "Phone 100% charge hone par madhur Hindi voice alert deta hai taaki battery surakshit rahe",
                    category = "Battery",
                    enabled = true,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.BATTERY_LEVEL,
                            summary = "Battery Level reaches 100%",
                            params = mapOf("level" to "100", "operator" to ">=")
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SPEAK_TEXT,
                            summary = "Awaaz: 'Dhyan dein! Battery 100% charge ho chuki hai...'",
                            params = mapOf("text" to "Dhyan dein! Phone ki battery 100 pratishat poori charge ho chuki hai. Kripya charger hata dein!")
                        ),
                        MacroAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            summary = "Notify: Battery 100% Full Alert",
                            params = mapOf("title" to "Micro Aman Alert", "message" to "Battery is 100% charged! Unplug charger.")
                        )
                    ),
                    constraints = listOf(
                        MacroConstraint(
                            type = ConstraintType.POWER_STATE,
                            summary = "Only when phone is charging",
                            params = mapOf("charging" to "true")
                        )
                    ),
                    colorTag = "#EF4444"
                ),
                Macro(
                    id = "default_charging_silent",
                    name = "Auto Silent on Phone Charging",
                    description = "Phone charging par lagane par automatically silent mode enable karta hai aur certified Hindi voice alert deta hai",
                    category = "Battery",
                    enabled = true,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.POWER_CONNECTED,
                            summary = "Phone Connected to Charger",
                            params = emptyMap()
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SET_RINGER_MODE,
                            summary = "Set Phone to Silent Mode 🔕",
                            params = mapOf("mode" to "SILENT")
                        ),
                        MacroAction(
                            type = ActionType.SPEAK_TEXT,
                            summary = "Awaaz: 'Phone charging shuru ho gayi hai, silent mode on...'",
                            params = mapOf("text" to "Phone charging shuru ho gayi hai. Device ko silent kar diya gaya hai.")
                        ),
                        MacroAction(
                            type = ActionType.SHOW_TOAST,
                            summary = "Toast: 'Charging: Phone Silenced 🔕'",
                            params = mapOf("message" to "Micro Aman: Phone on charging, Silent mode activated 🔕")
                        ),
                        MacroAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            summary = "Notify: Phone Silenced for Charging",
                            params = mapOf("title" to "Micro Aman Auto-Silent", "message" to "Charger connected: Phone automatically silenced 🔕")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#00E5FF"
                ),
                Macro(
                    id = "default_unplug_sound",
                    name = "Restore Sound on Charger Disconnected",
                    description = "Charger hatane par phone automatically normal sound mode par switch hota hai aur Hindi me batata hai",
                    category = "Battery",
                    enabled = true,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.POWER_DISCONNECTED,
                            summary = "Phone Disconnected from Charger",
                            params = emptyMap()
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SET_RINGER_MODE,
                            summary = "Restore Normal Sound 🔔",
                            params = mapOf("mode" to "NORMAL")
                        ),
                        MacroAction(
                            type = ActionType.SPEAK_TEXT,
                            summary = "Awaaz: 'Charger nikal diya gaya hai, awaaz normal ho gayi hai.'",
                            params = mapOf("text" to "Charger nikal diya gaya hai. Phone ki normal awaaz wapas chalu ho gayi hai.")
                        ),
                        MacroAction(
                            type = ActionType.SHOW_TOAST,
                            summary = "Toast: 'Normal Sound Restored 🔔'",
                            params = mapOf("message" to "Micro Aman: Normal sound mode restored 🔔")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#10B981"
                ),
                Macro(
                    id = "default_shake_flashlight",
                    name = "Shake Phone for Flashlight",
                    description = "Vigorously shake your device to toggle LED torch on or off instantly",
                    category = "Sensors",
                    enabled = true,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.SHAKE_DEVICE,
                            summary = "Shake Device (Accelerometer)",
                            params = mapOf("sensitivity" to "MEDIUM")
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.TOGGLE_FLASHLIGHT,
                            summary = "Toggle Flashlight / Torch",
                            params = mapOf("mode" to "TOGGLE")
                        ),
                        MacroAction(
                            type = ActionType.VIBRATE,
                            summary = "Quick haptic confirmation",
                            params = mapOf("pattern" to "SHORT")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#3B82F6"
                ),
                Macro(
                    id = "default_screen_unlock",
                    name = "Screen Unlock Welcome & Time",
                    description = "Device unlock hone par madhur Hindi me swagat aur sthiti batata hai",
                    category = "Utilities",
                    enabled = false,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.DEVICE_UNLOCKED,
                            summary = "Device Screen Unlocked",
                            params = emptyMap()
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SPEAK_TEXT,
                            summary = "Awaaz: 'Namaste Aman! Aapka swagat hai...'",
                            params = mapOf("text" to "Namaste Aman! Aapka swagat hai. Phone unlock ho gaya hai, sabhi automations taiyar hain!")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#10B981"
                ),
                Macro(
                    id = "default_low_battery_15",
                    name = "Low Battery Warning (15%)",
                    description = "Battery 15% se kam hone par Hindi me chetavani alert deta hai aur vibrate karta hai",
                    category = "Battery",
                    enabled = true,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.BATTERY_LEVEL,
                            summary = "Battery Level <= 15%",
                            params = mapOf("level" to "15", "operator" to "<=")
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SPEAK_TEXT,
                            summary = "Awaaz: 'Savdhaan! Phone ki battery 15% bachi hai...'",
                            params = mapOf("text" to "Savdhaan! Phone ki battery 15 pratishat bachi hai. Kripya apna phone turant charge karein.")
                        ),
                        MacroAction(
                            type = ActionType.VIBRATE,
                            summary = "SOS Double Pulse Vibration",
                            params = mapOf("pattern" to "DOUBLE_PULSE")
                        )
                    ),
                    constraints = listOf(
                        MacroConstraint(
                            type = ConstraintType.POWER_STATE,
                            summary = "Only when on battery (not charging)",
                            params = mapOf("charging" to "false")
                        )
                    ),
                    colorTag = "#EF4444"
                ),
                Macro(
                    id = "default_flip_silent",
                    name = "Flip Phone to Silent",
                    description = "Placing phone face down sets media and ringer volume to silent",
                    category = "Sensors",
                    enabled = false,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.FLIP_DEVICE,
                            summary = "Phone Placed Face Down",
                            params = emptyMap()
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SET_VOLUME,
                            summary = "Set Media Volume to 0%",
                            params = mapOf("volume" to "0")
                        ),
                        MacroAction(
                            type = ActionType.SHOW_TOAST,
                            summary = "Toast: 'Device silenced'",
                            params = mapOf("message" to "Micro Aman: Device silenced")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#7C3AED"
                ),
                Macro(
                    id = "default_headphones_plugged",
                    name = "Headphones Plugged Music Booster",
                    description = "Headphones connect hone par volume 60% set karta hai aur Hindi me batata hai",
                    category = "Device Hardware",
                    enabled = false,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.HEADPHONES_PLUGGED,
                            summary = "Headphones / Aux Cable Plugged In",
                            params = emptyMap()
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SET_VOLUME,
                            summary = "Set Media Volume to 60%",
                            params = mapOf("volume" to "60")
                        ),
                        MacroAction(
                            type = ActionType.SPEAK_TEXT,
                            summary = "Awaaz: 'Headphones jud gaye hain, gaane ka anand lein!'",
                            params = mapOf("text" to "Headphones jud gaye hain. Volume 60 pratishat kar diya gaya hai, sangeet ka maza lijiye!")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#00E5FF"
                ),
                Macro(
                    id = "default_whatsapp_auto_reply",
                    name = "WhatsApp Smart Auto-Reply",
                    description = "Automatically replies on WhatsApp when a message is received (can filter by specific number)",
                    category = "Messaging",
                    enabled = true,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.WHATSAPP_MESSAGE_RECEIVED,
                            summary = "Any WhatsApp message received",
                            params = mapOf("match_type" to "ALL")
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.WHATSAPP_AUTO_REPLY,
                            summary = "WhatsApp Auto-Reply: \"Namaste! Main abhi busy hoon...\"",
                            params = mapOf("reply_text" to "Namaste! Main abhi busy hoon, thodi der baad call ya message karta hoon. - Micro Aman Auto Reply")
                        ),
                        MacroAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            summary = "Notify: WhatsApp Auto-Reply Sent",
                            params = mapOf("title" to "Micro Aman Auto-Reply", "message" to "Auto-replied to WhatsApp sender")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#25D366"
                ),
                Macro(
                    id = "default_sms_auto_reply",
                    name = "SMS Auto-Reply: Busy / Driving Workflow",
                    description = "Incoming SMS message aane par automatically custom SMS reply bhejta hai (all contacts ya specific number)",
                    category = "Messaging",
                    enabled = true,
                    triggers = listOf(
                        MacroTrigger(
                            type = TriggerType.SMS_RECEIVED,
                            summary = "Any incoming SMS message received",
                            params = mapOf("match_type" to "ALL")
                        )
                    ),
                    actions = listOf(
                        MacroAction(
                            type = ActionType.SEND_SMS,
                            summary = "SMS Auto-Reply: \"Namaste! Main abhi busy hoon...\"",
                            params = mapOf("reply_text" to "Namaste! Main abhi busy hoon, thodi der baad call ya message karta hoon. - Micro Aman Auto Reply")
                        ),
                        MacroAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            summary = "Notify: SMS Auto-Reply Sent",
                            params = mapOf("title" to "Micro Aman SMS Auto-Reply", "message" to "Auto-replied to incoming SMS from {sender}")
                        )
                    ),
                    constraints = emptyList(),
                    colorTag = "#3B82F6"
                )
            )

            templates.forEach { saveMacro(it) }

            // Initial default variable
            saveVariable(Variable("user_name", "STRING", "Aman"))
            saveVariable(Variable("night_mode", "BOOLEAN", "false"))

            logEvent("System", "SYSTEM", "Micro Aman engine initialized with default automations", "INFO")
        }
        ensureEssentialMacrosExist()
    }

    // Ensures user has the latest auto-silent and auto-reply workflows available without any duplicate entries
    private suspend fun ensureEssentialMacrosExist() {
        val existing = macroDao.getAllMacrosOnce()
        val existingIds = existing.map { it.id }.toSet()
        val existingNames = existing.map { it.name.trim().lowercase() }.toSet()

        val essentials = listOf(
            Macro(
                id = "default_charging_silent",
                name = "Auto Silent on Phone Charging",
                description = "Phone charging par lagane par automatically silent mode enable karta hai aur certified Hindi voice alert deta hai",
                category = "Battery",
                enabled = true,
                triggers = listOf(
                    MacroTrigger(
                        type = TriggerType.POWER_CONNECTED,
                        summary = "Phone Connected to Charger",
                        params = emptyMap()
                    )
                ),
                actions = listOf(
                    MacroAction(
                        type = ActionType.SET_RINGER_MODE,
                        summary = "Set Phone to Silent Mode 🔕",
                        params = mapOf("mode" to "SILENT")
                    ),
                    MacroAction(
                        type = ActionType.SPEAK_TEXT,
                        summary = "Awaaz: 'Phone charging shuru ho gayi hai, silent mode on...'",
                        params = mapOf("text" to "Phone charging shuru ho gayi hai. Device ko silent kar diya gaya hai.")
                    ),
                    MacroAction(
                        type = ActionType.SHOW_TOAST,
                        summary = "Toast: 'Charging: Phone Silenced 🔕'",
                        params = mapOf("message" to "Micro Aman: Phone on charging, Silent mode activated 🔕")
                    ),
                    MacroAction(
                        type = ActionType.SHOW_NOTIFICATION,
                        summary = "Notify: Phone Silenced for Charging",
                        params = mapOf("title" to "Micro Aman Auto-Silent", "message" to "Charger connected: Phone automatically silenced 🔕")
                    )
                ),
                constraints = emptyList(),
                colorTag = "#00E5FF"
            ),
            Macro(
                id = "default_sms_auto_reply",
                name = "SMS Auto-Reply: Busy / Driving Workflow",
                description = "Incoming SMS message aane par automatically custom SMS reply bhejta hai (all contacts ya specific number)",
                category = "Messaging",
                enabled = true,
                triggers = listOf(
                    MacroTrigger(
                        type = TriggerType.SMS_RECEIVED,
                        summary = "Any incoming SMS message received",
                        params = mapOf("match_type" to "ALL")
                    )
                ),
                actions = listOf(
                    MacroAction(
                        type = ActionType.SEND_SMS,
                        summary = "SMS Auto-Reply: \"Namaste! Main abhi busy hoon...\"",
                        params = mapOf("reply_text" to "Namaste! Main abhi busy hoon, thodi der baad call ya message karta hoon. - Micro Aman Auto Reply")
                    ),
                    MacroAction(
                        type = ActionType.SHOW_NOTIFICATION,
                        summary = "Notify: SMS Auto-Reply Sent",
                        params = mapOf("title" to "Micro Aman SMS Auto-Reply", "message" to "Auto-replied to incoming SMS from {sender}")
                    )
                ),
                constraints = emptyList(),
                colorTag = "#3B82F6"
            )
        )

        for (m in essentials) {
            if (!existingIds.contains(m.id) && !existingNames.contains(m.name.trim().lowercase())) {
                saveMacro(m)
            }
        }
    }
}
