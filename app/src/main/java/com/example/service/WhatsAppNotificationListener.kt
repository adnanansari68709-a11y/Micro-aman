package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.MicroAmanApplication
import com.example.model.TriggerType

class WhatsAppNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""
        val messageBody = if (text.isNotBlank()) text else bigText

        // Filter out non-message / system WhatsApp alerts
        if (title.isBlank() || messageBody.isBlank()) return
        if (title.contains("WhatsApp", ignoreCase = true) && messageBody.contains("WhatsApp Web", ignoreCase = true)) return
        if (messageBody.contains("Checking for new messages", ignoreCase = true)) return
        if (messageBody.contains("Backup in progress", ignoreCase = true)) return
        if (messageBody.contains("Incoming voice call", ignoreCase = true) || messageBody.contains("Incoming video call", ignoreCase = true)) return

        // Extract Direct Reply action from notification
        extractAndCacheQuickReply(notification, packageName, title)

        // Dispatch trigger to automation engine
        val app = application as? MicroAmanApplication ?: return
        app.automationEngine.onTriggerFired(
            TriggerType.WHATSAPP_MESSAGE_RECEIVED,
            mapOf(
                "sender" to title,
                "message" to messageBody,
                "package" to packageName,
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }

    private fun extractAndCacheQuickReply(notification: Notification, packageName: String, sender: String) {
        // 1. Standard notification actions
        notification.actions?.forEach { action ->
            val remoteInputs = action.remoteInputs
            val intent = action.actionIntent
            if (!remoteInputs.isNullOrEmpty() && intent != null) {
                val directInput = remoteInputs.firstOrNull { it.resultKey.isNotBlank() }
                if (directInput != null) {
                    WhatsAppReplyManager.cacheReplyAction(
                        packageName = packageName,
                        sender = sender,
                        pendingIntent = intent,
                        resultKey = directInput.resultKey
                    )
                    return
                }
            }
        }

        // 2. WearableExtender fallback
        val wearable = NotificationCompat.WearableExtender(notification)
        for (action in wearable.actions) {
            val remoteInputs = action.remoteInputs
            val intent = action.actionIntent
            if (!remoteInputs.isNullOrEmpty() && intent != null) {
                val directInput = remoteInputs.firstOrNull { it.resultKey.isNotBlank() }
                if (directInput != null) {
                    WhatsAppReplyManager.cacheReplyAction(
                        packageName = packageName,
                        sender = sender,
                        pendingIntent = intent,
                        resultKey = directInput.resultKey
                    )
                    return
                }
            }
        }
    }
}
