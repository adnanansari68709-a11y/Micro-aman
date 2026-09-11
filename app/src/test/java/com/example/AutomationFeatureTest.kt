package com.example

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.MacroRepository
import com.example.engine.AutomationEngine
import com.example.model.ActionType
import com.example.model.AutomationLog
import com.example.model.Macro
import com.example.model.MacroAction
import com.example.model.MacroTrigger
import com.example.model.TriggerType
import com.example.ui.screens.templateCatalog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AutomationFeatureTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: MacroRepository
    private lateinit var engine: AutomationEngine

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(context)
        repository = MacroRepository(database.macroDao(), database.logDao(), database.variableDao())
        engine = AutomationEngine(context, repository)
        engine.setMasterEnabled(true)
    }

    @Test
    fun testMacroSerializationWithSmsAndRingerActions() {
        val macro = Macro(
            id = "test_charging_macro",
            name = "Test Charging Silent",
            description = "Silences on charge",
            category = "Battery",
            enabled = true,
            triggers = listOf(
                MacroTrigger(
                    type = TriggerType.POWER_CONNECTED,
                    summary = "Power Connected",
                    params = emptyMap()
                ),
                MacroTrigger(
                    type = TriggerType.SMS_RECEIVED,
                    summary = "SMS from contact",
                    params = mapOf("match_type" to "SPECIFIC_CONTACT", "contact" to "+919876543210")
                )
            ),
            actions = listOf(
                MacroAction(
                    type = ActionType.SET_RINGER_MODE,
                    summary = "Set Phone to Silent Mode 🔕",
                    params = mapOf("mode" to "SILENT")
                ),
                MacroAction(
                    type = ActionType.SEND_SMS,
                    summary = "Send SMS Reply",
                    params = mapOf("reply_text" to "I am busy right now, {sender}!")
                )
            )
        )

        val json = macro.toJson()
        val restored = Macro.fromJson(json)

        assertEquals("test_charging_macro", restored.id)
        assertEquals(2, restored.triggers.size)
        assertEquals(TriggerType.POWER_CONNECTED, restored.triggers[0].type)
        assertEquals(TriggerType.SMS_RECEIVED, restored.triggers[1].type)
        assertEquals("SPECIFIC_CONTACT", restored.triggers[1].params["match_type"])
        assertEquals("+919876543210", restored.triggers[1].params["contact"])

        assertEquals(2, restored.actions.size)
        assertEquals(ActionType.SET_RINGER_MODE, restored.actions[0].type)
        assertEquals("SILENT", restored.actions[0].params["mode"])
        assertEquals(ActionType.SEND_SMS, restored.actions[1].type)
        assertEquals("I am busy right now, {sender}!", restored.actions[1].params["reply_text"])
    }

    @Test
    fun testChargingSilentTemplateExistsInCatalog() {
        val chargingSilentTemplate = templateCatalog.find { it.macro.name == "Auto Silent on Phone Charging" }
        assertNotNull("Auto Silent on Phone Charging template must exist in catalog", chargingSilentTemplate)
        assertEquals(TriggerType.POWER_CONNECTED, chargingSilentTemplate!!.macro.triggers[0].type)
        assertTrue(chargingSilentTemplate.macro.actions.any { it.type == ActionType.SET_RINGER_MODE && it.params["mode"] == "SILENT" })

        val normalSoundTemplate = templateCatalog.find { it.macro.name == "Restore Sound on Charger Disconnected" }
        assertNotNull("Restore Sound template must exist in catalog", normalSoundTemplate)
        assertEquals(TriggerType.POWER_DISCONNECTED, normalSoundTemplate!!.macro.triggers[0].type)
        assertTrue(normalSoundTemplate.macro.actions.any { it.type == ActionType.SET_RINGER_MODE && it.params["mode"] == "NORMAL" })
    }

    @Test
    fun testSmsAutoReplyTemplateExistsInCatalog() {
        val smsTemplate = templateCatalog.find { it.macro.name == "SMS Auto-Reply Workflow" }
        assertNotNull("SMS Auto-Reply Workflow template must exist in catalog", smsTemplate)
        assertEquals(TriggerType.SMS_RECEIVED, smsTemplate!!.macro.triggers[0].type)
        assertTrue(smsTemplate.macro.actions.any { it.type == ActionType.SEND_SMS })
    }

    @Test
    fun testSmsTriggerContactMatching() = runBlocking {
        // Create macro filtering for specific number
        val macro = Macro(
            id = "specific_contact_reply",
            name = "VIP SMS Reply",
            description = "Auto reply for VIP number",
            enabled = true,
            triggers = listOf(
                MacroTrigger(
                    type = TriggerType.SMS_RECEIVED,
                    summary = "From VIP",
                    params = mapOf("match_type" to "SPECIFIC_CONTACT", "contact" to "+919876543210")
                )
            ),
            actions = listOf(
                MacroAction(
                    type = ActionType.SHOW_TOAST,
                    summary = "Toast",
                    params = mapOf("message" to "VIP SMS processed")
                )
            )
        )
        repository.saveMacro(macro)

        // Matching sender: "+919876543210" or normalized "9876543210"
        engine.processTrigger(
            TriggerType.SMS_RECEIVED,
            mapOf("sender" to "+919876543210", "message" to "Urgent meeting")
        )

        val logs = repository.getAllLogsOnce()
        val matchedLog = logs.find { it.macroName == "VIP SMS Reply" && it.eventType == "TRIGGER" }
        assertNotNull("VIP macro should have been triggered for matching sender", matchedLog)

        // Non-matching sender: "+911111111111"
        engine.processTrigger(
            TriggerType.SMS_RECEIVED,
            mapOf("sender" to "+911111111111", "message" to "Hey")
        )

        val nonMatchedLogs = repository.getAllLogsOnce().filter { it.macroName == "VIP SMS Reply" && it.eventType == "TRIGGER" }
        assertEquals("Should not trigger for non-matching sender", 1, nonMatchedLogs.size)
    }

    @Test
    fun testPowerConnectedTriggerActivatesRingerModeSilent() = runBlocking {
        val macro = Macro(
            id = "charging_silent_test",
            name = "Test Charging Silent Execution",
            description = "Silent when charging",
            enabled = true,
            triggers = listOf(
                MacroTrigger(type = TriggerType.POWER_CONNECTED, summary = "Plugged in")
            ),
            actions = listOf(
                MacroAction(
                    type = ActionType.SET_RINGER_MODE,
                    summary = "Silent mode",
                    params = mapOf("mode" to "SILENT")
                )
            )
        )
        repository.saveMacro(macro)

        // Simulate power connected
        engine.processTrigger(TriggerType.POWER_CONNECTED, emptyMap())

        val logs = repository.getAllLogsOnce()
        val actionLog = logs.find { it.macroName == "Test Charging Silent Execution" && it.eventType == "ACTION" }
        assertNotNull("Action log for SET_RINGER_MODE should be present", actionLog)
        assertTrue("Log should confirm silent mode", actionLog!!.detail.contains("Silent Mode"))
    }

    @Test
    fun testSmsAutoReplyExecutionWithTokenInterpolation() = runBlocking {
        val macro = Macro(
            id = "sms_token_test",
            name = "Test Token Interpolation",
            description = "Token interpolation",
            enabled = true,
            triggers = listOf(
                MacroTrigger(type = TriggerType.SMS_RECEIVED, summary = "Any SMS", params = mapOf("match_type" to "ALL"))
            ),
            actions = listOf(
                MacroAction(
                    type = ActionType.SEND_SMS,
                    summary = "Send SMS",
                    params = mapOf("reply_text" to "Hello {sender}, I got your message '{message}'.")
                )
            )
        )
        repository.saveMacro(macro)

        engine.processTrigger(
            TriggerType.SMS_RECEIVED,
            mapOf("sender" to "+919876543210", "message" to "Are you free?")
        )

        val logs = repository.getAllLogsOnce()
        val actionLog = logs.find { it.macroName == "Test Token Interpolation" && it.eventType == "ACTION" }
        assertNotNull("Action log for SEND_SMS should exist", actionLog)
        assertTrue("Tokens {sender} and {message} should be interpolated",
            actionLog!!.detail.contains("+919876543210") && actionLog.detail.contains("Are you free?"))
    }

    @Test
    fun testDeduplicateMacrosRemovesDuplicates() = runBlocking {
        val macro1 = Macro(
            id = "dup_1",
            name = "Battery Alarm Hindi",
            description = "First",
            enabled = true
        )
        val macro2 = Macro(
            id = "dup_2",
            name = "Battery Alarm Hindi",
            description = "Duplicate",
            enabled = true
        )
        val legacy1 = Macro(
            id = "default_charger_connected",
            name = "Charger Connected Voice",
            description = "Legacy",
            enabled = true
        )
        val unified = Macro(
            id = "default_charging_silent",
            name = "Auto Silent on Phone Charging",
            description = "Unified single feature",
            enabled = true
        )
        repository.saveMacro(macro1)
        repository.saveMacro(macro2)
        repository.saveMacro(legacy1)
        repository.saveMacro(unified)

        repository.deduplicateMacros()

        val all = database.macroDao().getAllMacrosOnce()
        // Legacy charger_connected should be removed since default_charging_silent exists
        assertFalse("Legacy duplicate default_charger_connected should be removed", all.any { it.id == "default_charger_connected" })
        assertTrue("Unified charging silent should remain", all.any { it.id == "default_charging_silent" })
        // Duplicate name should only appear once
        val matchingNames = all.filter { it.name.trim().lowercase() == "battery alarm hindi" }
        assertEquals("Duplicate named macro should be deduplicated to 1", 1, matchingNames.size)
    }
}
