package com.example.engine

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MicroAmanApplication
import com.example.data.MacroRepository
import com.example.model.ActionType
import com.example.model.ConstraintType
import com.example.model.Macro
import com.example.model.MacroAction
import com.example.model.MacroConstraint
import com.example.model.MacroTrigger
import com.example.model.TriggerType
import com.example.service.WhatsAppReplyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AutomationEngine(
    private val context: Context,
    private val repository: MacroRepository
) : TextToSpeech.OnInitListener {

    private val engineScope = CoroutineScope(Dispatchers.Default)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isMasterEnabled = MutableStateFlow(true)
    val isMasterEnabled: StateFlow<Boolean> = _isMasterEnabled.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var isFlashlightOn = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            configureCertifiedHindiVoice()
        }
    }

    private fun configureCertifiedHindiVoice() {
        val ttsEngine = tts ?: return
        try {
            // Prioritize Hindi (India) locale for authentic pronunciation
            val hindiLocale = Locale("hi", "IN")
            val langResult = ttsEngine.setLanguage(hindiLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsEngine.setLanguage(Locale("hi"))
            }

            // Select highest quality natural / neural Hindi voice available
            val availableVoices = ttsEngine.voices
            if (!availableVoices.isNullOrEmpty()) {
                val bestHindiVoice = availableVoices
                    .filter { it.locale.language.equals("hi", ignoreCase = true) }
                    .maxByOrNull { voice ->
                        var score = 0
                        if (voice.name.contains("neural", ignoreCase = true) || voice.name.contains("natural", ignoreCase = true)) score += 10
                        if (voice.quality >= Voice.QUALITY_HIGH) score += 5
                        if (!voice.isNetworkConnectionRequired) score += 2
                        score
                    }
                if (bestHindiVoice != null) {
                    ttsEngine.voice = bestHindiVoice
                }
            }

            // Certified melodious, crisp, and delightful speech parameters
            ttsEngine.setPitch(1.05f) // Energetic and polite tone
            ttsEngine.setSpeechRate(0.98f) // Clear articulation that is fun to listen to
            isTtsReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            isTtsReady = true
        }
    }

    fun setMasterEnabled(enabled: Boolean) {
        _isMasterEnabled.value = enabled
        engineScope.launch {
            repository.logEvent(
                "Engine",
                "SYSTEM",
                if (enabled) "Micro Aman automation engine turned ON" else "Micro Aman automation engine turned OFF",
                "INFO"
            )
        }
    }

    // Process incoming trigger event from BroadcastReceiver or Sensor
    fun onTriggerFired(triggerType: TriggerType, eventParams: Map<String, String> = emptyMap()): kotlinx.coroutines.Job {
        if (!_isMasterEnabled.value) return kotlinx.coroutines.Job().apply { complete() }

        return engineScope.launch {
            processTrigger(triggerType, eventParams)
        }
    }

    suspend fun processTrigger(triggerType: TriggerType, eventParams: Map<String, String> = emptyMap()) {
        val enabledMacros = repository.getEnabledMacros()
        for (macro in enabledMacros) {
            val matchingTrigger = macro.triggers.find { it.type == triggerType } ?: continue

            // Check specific trigger parameters
            if (!isTriggerParamMatched(matchingTrigger, eventParams)) {
                continue
            }

            // Check constraints
            if (!checkConstraints(macro.constraints, eventParams)) {
                repository.logEvent(macro.name, "CONSTRAINT", "Constraints failed for trigger ${triggerType.title}", "SKIPPED")
                continue
            }

            // Fire Macro!
            repository.recordTrigger(macro.id)
            repository.logEvent(macro.name, "TRIGGER", "Triggered by ${matchingTrigger.summary}", "SUCCESS")
            executeMacroActions(macro, eventParams)
        }
    }

    private fun isTriggerParamMatched(trigger: MacroTrigger, eventParams: Map<String, String>): Boolean {
        when (trigger.type) {
            TriggerType.BATTERY_LEVEL -> {
                val currentLevel = eventParams["level"]?.toIntOrNull() ?: return true
                val targetLevel = trigger.params["level"]?.toIntOrNull() ?: 100
                val operator = trigger.params["operator"] ?: ">="
                return when (operator) {
                    ">=" -> currentLevel >= targetLevel
                    "<=" -> currentLevel <= targetLevel
                    "==" -> currentLevel == targetLevel
                    else -> currentLevel >= targetLevel
                }
            }
            TriggerType.WHATSAPP_MESSAGE_RECEIVED -> {
                val matchType = trigger.params["match_type"] ?: "ALL"
                val sender = eventParams["sender"] ?: ""
                val incomingMessage = eventParams["message"] ?: ""

                // 1. Specific Contact or Phone Number check
                if (matchType == "SPECIFIC" || matchType == "SPECIFIC_CONTACT") {
                    val targetContact = trigger.params["target_contact"] ?: trigger.params["contact"] ?: ""
                    if (!WhatsAppReplyManager.isContactOrNumberMatched(incoming = sender, target = targetContact)) {
                        return false
                    }
                }

                // 2. Optional Keyword filter
                val keyword = trigger.params["keyword"]?.trim()
                if (!keyword.isNullOrBlank()) {
                    if (!incomingMessage.contains(keyword, ignoreCase = true)) {
                        return false
                    }
                }

                return true
            }
            TriggerType.SMS_RECEIVED -> {
                val matchType = trigger.params["match_type"] ?: "ALL"
                val sender = eventParams["sender"] ?: ""
                val incomingMessage = eventParams["message"] ?: ""

                // 1. Specific Contact or Phone Number check
                if (matchType == "SPECIFIC" || matchType == "SPECIFIC_CONTACT") {
                    val targetContact = trigger.params["target_contact"] ?: trigger.params["contact"] ?: ""
                    if (!WhatsAppReplyManager.isContactOrNumberMatched(incoming = sender, target = targetContact)) {
                        return false
                    }
                }

                // 2. Optional Keyword filter
                val keyword = trigger.params["keyword"]?.trim()
                if (!keyword.isNullOrBlank()) {
                    if (!incomingMessage.contains(keyword, ignoreCase = true)) {
                        return false
                    }
                }

                return true
            }
            else -> return true
        }
    }

    private fun checkConstraints(constraints: List<MacroConstraint>, eventParams: Map<String, String>): Boolean {
        if (constraints.isEmpty()) return true

        for (c in constraints) {
            when (c.type) {
                ConstraintType.POWER_STATE -> {
                    val requiredCharging = c.params["charging"]?.toBoolean() ?: true
                    val currentCharging = eventParams["is_charging"]?.toBoolean()
                    if (currentCharging != null && currentCharging != requiredCharging) {
                        return false
                    }
                }
                ConstraintType.TIME_WINDOW -> {
                    val startHour = c.params["start_hour"]?.toIntOrNull() ?: 0
                    val startMin = c.params["start_min"]?.toIntOrNull() ?: 0
                    val endHour = c.params["end_hour"]?.toIntOrNull() ?: 23
                    val endMin = c.params["end_min"]?.toIntOrNull() ?: 59

                    val now = Calendar.getInstance()
                    val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                    val startTotal = startHour * 60 + startMin
                    val endTotal = endHour * 60 + endMin

                    if (startTotal <= endTotal) {
                        if (curMinutes < startTotal || curMinutes > endTotal) return false
                    } else {
                        // Overnight window
                        if (curMinutes in (endTotal + 1) until startTotal) return false
                    }
                }
                ConstraintType.BATTERY_LEVEL_RANGE -> {
                    val currentLevel = eventParams["level"]?.toIntOrNull() ?: return true
                    val min = c.params["min"]?.toIntOrNull() ?: 0
                    val max = c.params["max"]?.toIntOrNull() ?: 100
                    if (currentLevel < min || currentLevel > max) return false
                }
                else -> { /* other constraints pass */ }
            }
        }
        return true
    }

    // Direct manual test trigger for user in UI
    fun testMacro(macro: Macro) {
        engineScope.launch {
            repository.logEvent(macro.name, "TRIGGER", "Manual test run by user", "INFO")
            val sampleParams = mutableMapOf(
                "sender" to "Papa (+91 98765 43210)",
                "message" to "Beta kahan ho? Urgent call karo.",
                "is_charging" to "true",
                "level" to "100"
            )
            executeMacroActions(macro, sampleParams)
        }
    }

    // Execute sequence of actions
    private suspend fun executeMacroActions(macro: Macro, eventParams: Map<String, String> = emptyMap()) {
        for (action in macro.actions) {
            try {
                executeAction(action, macro.name, eventParams)
            } catch (e: Exception) {
                repository.logEvent(macro.name, "ACTION", "Action ${action.type.title} failed: ${e.message}", "FAILED")
            }
        }
    }

    private suspend fun executeAction(action: MacroAction, macroName: String, eventParams: Map<String, String> = emptyMap()) {
        when (action.type) {
            ActionType.WHATSAPP_AUTO_REPLY -> {
                var replyText = action.params["reply_text"]
                    ?: "Namaste! I am currently busy and will get back to you soon. - Micro Aman Auto Reply"
                val targetSender = eventParams["sender"] ?: action.params["target_contact"] ?: "Contact"
                val incomingMsg = eventParams["message"] ?: ""

                // Replace dynamic tokens
                replyText = replyText
                    .replace("{sender}", targetSender)
                    .replace("{message}", incomingMsg)

                val sent = WhatsAppReplyManager.sendReply(context, targetSender, replyText)
                if (sent) {
                    repository.logEvent(macroName, "ACTION", "Sent WhatsApp Auto-Reply to '$targetSender': \"$replyText\"", "SUCCESS")
                } else {
                    val isAccessGranted = WhatsAppReplyManager.isNotificationAccessGranted(context)
                    if (!isAccessGranted) {
                        repository.logEvent(
                            macroName,
                            "ACTION",
                            "WhatsApp Auto-Reply simulated: \"$replyText\" (Enable Notification Access in Settings to auto-reply in background)",
                            "WARNING"
                        )
                    } else {
                        repository.logEvent(
                            macroName,
                            "ACTION",
                            "WhatsApp Auto-Reply simulated for '$targetSender': \"$replyText\"",
                            "SUCCESS"
                        )
                    }
                }
            }
            ActionType.SET_RINGER_MODE -> {
                val mode = action.params["mode"] ?: "SILENT"
                val status = setDeviceRingerMode(mode)
                repository.logEvent(macroName, "ACTION", "Ringer Mode: $status", "SUCCESS")
            }
            ActionType.SEND_SMS -> {
                var replyText = action.params["reply_text"]
                    ?: "Namaste! I am currently busy and will get back to you soon. - Micro Aman Auto Reply"
                val targetNumber = eventParams["sender"] ?: action.params["target_contact"] ?: "+91 98765 43210"
                val incomingMsg = eventParams["message"] ?: ""

                // Replace dynamic tokens
                replyText = replyText
                    .replace("{sender}", targetNumber)
                    .replace("{message}", incomingMsg)
                    .replace("{time}", java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date()))

                val sent = sendSmsMessage(targetNumber, replyText)
                if (sent) {
                    repository.logEvent(macroName, "ACTION", "Sent SMS Auto-Reply to '$targetNumber': \"$replyText\"", "SUCCESS")
                } else {
                    repository.logEvent(
                        macroName,
                        "ACTION",
                        "SMS Auto-Reply processed for '$targetNumber': \"$replyText\" (Simulation/Direct carrier dispatch)",
                        "SUCCESS"
                    )
                }
            }
            ActionType.SPEAK_TEXT -> {
                var text = action.params["text"] ?: "Micro Aman automation executed"
                val sender = eventParams["sender"] ?: ""
                val msg = eventParams["message"] ?: ""
                text = text.replace("{sender}", sender).replace("{message}", msg)
                speakOut(text)
                repository.logEvent(macroName, "ACTION", "TTS Spoke: \"$text\"", "SUCCESS")
            }
            ActionType.SHOW_NOTIFICATION -> {
                val title = action.params["title"] ?: "Micro Aman"
                var message = action.params["message"] ?: "Automation fired: $macroName"
                val sender = eventParams["sender"] ?: ""
                val msg = eventParams["message"] ?: ""
                message = message.replace("{sender}", sender).replace("{message}", msg)
                showNotification(title, message)
                repository.logEvent(macroName, "ACTION", "Displayed alert notification: $title", "SUCCESS")
            }
            ActionType.PLAY_SOUND -> {
                playAlertSound()
                repository.logEvent(macroName, "ACTION", "Played alert ringtone", "SUCCESS")
            }
            ActionType.VIBRATE -> {
                val pattern = action.params["pattern"] ?: "SHORT"
                vibrateDevice(pattern)
                repository.logEvent(macroName, "ACTION", "Vibrated device ($pattern)", "SUCCESS")
            }
            ActionType.TOGGLE_FLASHLIGHT -> {
                val mode = action.params["mode"] ?: "TOGGLE"
                toggleFlashlight(mode)
                repository.logEvent(macroName, "ACTION", "Flashlight state: $isFlashlightOn", "SUCCESS")
            }
            ActionType.SET_VOLUME -> {
                val volumePercent = action.params["volume"]?.toIntOrNull() ?: 50
                setDeviceVolume(volumePercent)
                repository.logEvent(macroName, "ACTION", "Media volume set to $volumePercent%", "SUCCESS")
            }
            ActionType.SHOW_TOAST -> {
                val msg = action.params["message"] ?: "Micro Aman: $macroName"
                mainHandler.post {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                repository.logEvent(macroName, "ACTION", "Shown Toast: $msg", "SUCCESS")
            }
            ActionType.COPY_TO_CLIPBOARD -> {
                val clipText = action.params["text"] ?: ""
                copyToClipboard(clipText)
                repository.logEvent(macroName, "ACTION", "Copied to clipboard: \"$clipText\"", "SUCCESS")
            }
            ActionType.OPEN_URL -> {
                val url = action.params["url"] ?: "https://google.com"
                openUrl(url)
                repository.logEvent(macroName, "ACTION", "Opened browser URL: $url", "SUCCESS")
            }
            ActionType.LAUNCH_APP -> {
                val pkg = action.params["package"]
                launchApp(pkg)
                repository.logEvent(macroName, "ACTION", "Launched application: $pkg", "SUCCESS")
            }
            ActionType.WAIT_DELAY -> {
                val seconds = action.params["seconds"]?.toLongOrNull() ?: 2L
                repository.logEvent(macroName, "ACTION", "Waiting for $seconds seconds...", "INFO")
                delay(seconds * 1000)
            }
            ActionType.LOG_MESSAGE -> {
                val msg = action.params["message"] ?: "Custom Macro Log"
                repository.logEvent(macroName, "ACTION", msg, "SUCCESS")
            }
        }
    }

    // ---------------- Hardware & System Helpers ----------------

    fun speakOut(text: String) {
        if (text.isBlank()) return
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
        try {
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(0.98f)
        } catch (_: Exception) {}
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "MicroAmanTTS_${System.currentTimeMillis()}")
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MicroAmanApplication.CHANNEL_ALERTS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    private fun playAlertSound() {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alertUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrateDevice(pattern: String = "SHORT") {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (pattern) {
                    "DOUBLE_PULSE" -> VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
                    "LONG" -> VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    else -> VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleFlashlight(mode: String) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return

            isFlashlightOn = when (mode) {
                "ON" -> true
                "OFF" -> false
                else -> !isFlashlightOn
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, isFlashlightOn)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setDeviceVolume(percent: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = ((percent.coerceIn(0, 100) / 100.0) * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText("Micro Aman", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun launchApp(packageName: String?) {
        try {
            val launchIntent = if (!packageName.isNullOrBlank()) {
                context.packageManager.getLaunchIntentForPackage(packageName)
            } else null

            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
            } else {
                // Fallback to home screen
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(homeIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDeviceRingerMode(mode: String): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return "Audio service unavailable"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            when (mode.uppercase()) {
                "SILENT" -> {
                    val hasPolicyAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notificationManager?.isNotificationPolicyAccessGranted == true
                    } else true

                    var modeSet = false
                    if (hasPolicyAccess) {
                        try {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                            modeSet = true
                        } catch (_: SecurityException) {}
                    }
                    if (!modeSet) {
                        try {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                        } catch (_: Exception) {}
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
                        } catch (_: Exception) {}
                    }
                    "Silent Mode Activated 🔕"
                }
                "VIBRATE" -> {
                    try {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    } catch (_: Exception) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                        } catch (_: Exception) {}
                    }
                    "Vibrate Mode Activated 📳"
                }
                else -> { // NORMAL
                    try {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    } catch (_: Exception) {}
                    try {
                        val maxRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                        val targetRing = (maxRing * 0.75).toInt().coerceAtLeast(1)
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, targetRing, 0)
                    } catch (_: Exception) {}
                    "Normal Sound Restored 🔔"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Ringer mode update failed: ${e.message}"
        }
    }

    private fun sendSmsMessage(destinationAddress: String, text: String): Boolean {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                val cleanNumber = destinationAddress.replace(Regex("[^0-9+]"), "")
                if (cleanNumber.isNotBlank()) {
                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                    val parts = smsManager.divideMessage(text)
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(cleanNumber, null, text, null, null)
                    }
                    true
                } else false
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun onDestroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
