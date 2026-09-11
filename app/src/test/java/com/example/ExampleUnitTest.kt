package com.example

import com.example.model.ActionType
import com.example.model.MacroAction
import com.example.model.MacroTrigger
import com.example.model.TriggerType
import com.example.service.WhatsAppReplyManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testWhatsAppContactMatching_exactAndCaseInsensitive() {
        assertTrue(WhatsAppReplyManager.isContactOrNumberMatched("Aman", "Aman"))
        assertTrue(WhatsAppReplyManager.isContactOrNumberMatched("aman sharma", "Aman Sharma"))
        assertTrue(WhatsAppReplyManager.isContactOrNumberMatched("Aman Sharma (3 new messages)", "Aman Sharma"))
    }

    @Test
    fun testWhatsAppPhoneNumberMatching_withCountryCodeAndFormatting() {
        // Both contain 10-digit base number 9876543210
        assertTrue(WhatsAppReplyManager.isContactOrNumberMatched("+91 98765 43210", "9876543210"))
        assertTrue(WhatsAppReplyManager.isContactOrNumberMatched("+919876543210", "+91 98765-43210"))
        assertTrue(WhatsAppReplyManager.isContactOrNumberMatched("Papa (+91 9876543210)", "9876543210"))
        assertTrue(WhatsAppReplyManager.isContactOrNumberMatched("Papa (+91 9876543210)", "Papa"))
    }

    @Test
    fun testWhatsAppMatching_negativeCase() {
        assertFalse(WhatsAppReplyManager.isContactOrNumberMatched("Rahul", "Aman"))
        assertFalse(WhatsAppReplyManager.isContactOrNumberMatched("+91 98111 22233", "9876543210"))
    }

    @Test
    fun testWhatsAppMacroAction_tagReplacement() {
        val template = "Namaste {sender}! I am busy. Received: \"{message}\""
        val sender = "Rohan (+91 98765 43210)"
        val message = "Kal subah meeting hai?"

        val formatted = template.replace("{sender}", sender).replace("{message}", message)

        assertTrue(formatted.contains("Namaste Rohan (+91 98765 43210)!"))
        assertTrue(formatted.contains("Kal subah meeting hai?"))
    }

    @Test
    fun testWhatsAppTriggerAndActionDataModels() {
        val trigger = MacroTrigger(
            type = TriggerType.WHATSAPP_MESSAGE_RECEIVED,
            summary = "WhatsApp message from Ramesh",
            params = mapOf("match_type" to "SPECIFIC", "target_contact" to "Ramesh", "keyword" to "urgent")
        )
        assertEquals(TriggerType.WHATSAPP_MESSAGE_RECEIVED, trigger.type)
        assertEquals("SPECIFIC", trigger.params["match_type"])
        assertEquals("Ramesh", trigger.params["target_contact"])

        val action = MacroAction(
            type = ActionType.WHATSAPP_AUTO_REPLY,
            summary = "WhatsApp Auto-Reply",
            params = mapOf("reply_text" to "I will call you back soon.")
        )
        assertEquals(ActionType.WHATSAPP_AUTO_REPLY, action.type)
        assertEquals("I will call you back soon.", action.params["reply_text"])
    }
}

