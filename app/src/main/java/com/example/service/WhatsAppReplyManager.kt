package com.example.service

import android.app.PendingIntent
import android.app.RemoteInput
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import com.example.MicroAmanApplication
import com.example.model.TriggerType
import java.util.concurrent.ConcurrentHashMap

data class CachedReplyAction(
    val pendingIntent: PendingIntent,
    val resultKey: String,
    val packageName: String,
    val originalSender: String,
    val timestamp: Long = System.currentTimeMillis()
)

object WhatsAppReplyManager {

    private val cachedActions = ConcurrentHashMap<String, CachedReplyAction>()
    @Volatile
    private var lastCachedAction: CachedReplyAction? = null

    /**
     * Checks if NotificationListenerService access has been granted by user in Android Settings.
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        if (TextUtils.isEmpty(flat)) return false
        val names = flat.split(":")
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                return true
            }
        }
        return false
    }

    /**
     * Opens system notification access settings page.
     */
    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Caches the quick reply PendingIntent extracted from a WhatsApp notification.
     */
    fun cacheReplyAction(
        packageName: String,
        sender: String,
        pendingIntent: PendingIntent,
        resultKey: String
    ) {
        val action = CachedReplyAction(
            pendingIntent = pendingIntent,
            resultKey = resultKey,
            packageName = packageName,
            originalSender = sender
        )
        lastCachedAction = action
        val key = normalizeKey(sender)
        cachedActions[key] = action

        // Also index by phone digits if sender contains a phone number
        val digits = sender.filter { it.isDigit() }
        if (digits.length >= 6) {
            cachedActions[digits.takeLast(10)] = action
        }
    }

    /**
     * Sends auto-reply text via Android RemoteInput mechanism back to WhatsApp.
     */
    fun sendReply(context: Context, targetSender: String?, replyText: String): Boolean {
        val action = findActionForSender(targetSender) ?: lastCachedAction ?: return false

        return try {
            val replyIntent = Intent()
            val bundle = Bundle()
            bundle.putCharSequence(action.resultKey, replyText)

            val remoteInput = RemoteInput.Builder(action.resultKey).build()
            RemoteInput.addResultsToIntent(arrayOf(remoteInput), replyIntent, bundle)

            action.pendingIntent.send(context, 0, replyIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun findActionForSender(sender: String?): CachedReplyAction? {
        if (sender.isNullOrBlank()) return lastCachedAction

        val normalized = normalizeKey(sender)
        cachedActions[normalized]?.let { return it }

        val digits = sender.filter { it.isDigit() }
        if (digits.length >= 6) {
            val last10 = digits.takeLast(10)
            cachedActions[last10]?.let { return it }
        }

        // Fuzzy match on cached sender names
        for ((k, action) in cachedActions) {
            if (isContactOrNumberMatched(incoming = action.originalSender, target = sender)) {
                return action
            }
        }

        return lastCachedAction
    }

    fun normalizeKey(input: String): String {
        return input.trim().lowercase()
    }

    /**
     * Helper for matching incoming sender with configured target contact or number.
     * Supports:
     * - Name match (case-insensitive substring)
     * - Phone number digits match (+91 98765 43210 vs 9876543210 or last 10 digits)
     */
    fun isContactOrNumberMatched(incoming: String, target: String): Boolean {
        val cleanTarget = target.trim()
        val cleanIncoming = incoming.trim()
        if (cleanTarget.isBlank()) return true

        // Direct case-insensitive match
        if (cleanIncoming.equals(cleanTarget, ignoreCase = true)) {
            return true
        }

        // Substring name match (e.g. "Aman" matches "Aman Sharma")
        if (cleanIncoming.contains(cleanTarget, ignoreCase = true) ||
            cleanTarget.contains(cleanIncoming, ignoreCase = true)) {
            return true
        }

        // Digits / Phone number match
        val incomingDigits = cleanIncoming.filter { it.isDigit() }
        val targetDigits = cleanTarget.filter { it.isDigit() }
        if (targetDigits.length >= 6 && incomingDigits.length >= 6) {
            val targetLast10 = targetDigits.takeLast(10)
            val incomingLast10 = incomingDigits.takeLast(10)
            if (targetLast10 == incomingLast10) return true
            if (incomingDigits.contains(targetDigits) || targetDigits.contains(incomingDigits)) {
                return true
            }
        }

        return false
    }

    /**
     * Testing / simulation utility: triggers a simulated WhatsApp message event.
     */
    fun simulateIncomingWhatsAppMessage(
        context: Context,
        sender: String = "Aman (+91 98765 43210)",
        message: String = "Hi, are you free right now?"
    ) {
        val app = context.applicationContext as? MicroAmanApplication ?: return
        app.automationEngine.onTriggerFired(
            TriggerType.WHATSAPP_MESSAGE_RECEIVED,
            mapOf(
                "sender" to sender,
                "message" to message,
                "package" to "com.whatsapp",
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }
}
