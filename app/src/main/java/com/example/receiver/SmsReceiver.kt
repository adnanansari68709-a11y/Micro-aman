package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import com.example.MicroAmanApplication
import com.example.model.TriggerType

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val extras: Bundle? = intent.extras
        if (extras == null) return

        try {
            val pdus = extras.get("pdus") as? Array<*> ?: return
            val format = extras.getString("format")

            var sender = ""
            val fullMessage = StringBuilder()

            for (pdu in pdus) {
                val bytes = pdu as? ByteArray ?: continue
                val smsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(bytes, format)
                } else {
                    @Suppress("DEPRECATION")
                    SmsMessage.createFromPdu(bytes)
                }

                if (smsMessage != null) {
                    if (sender.isBlank()) {
                        sender = smsMessage.displayOriginatingAddress ?: smsMessage.originatingAddress ?: ""
                    }
                    fullMessage.append(smsMessage.displayMessageBody ?: smsMessage.messageBody ?: "")
                }
            }

            val finalSender = sender.trim()
            val messageText = fullMessage.toString().trim()

            if (finalSender.isNotBlank() || messageText.isNotBlank()) {
                val app = context.applicationContext as? MicroAmanApplication
                app?.automationEngine?.onTriggerFired(
                    TriggerType.SMS_RECEIVED,
                    mapOf(
                        "sender" to finalSender,
                        "message" to messageText,
                        "timestamp" to System.currentTimeMillis().toString()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
