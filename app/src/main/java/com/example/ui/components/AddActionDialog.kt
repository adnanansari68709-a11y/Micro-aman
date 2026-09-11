package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.MacroAction
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.AmanCyan
import com.example.ui.theme.AmanDarkBorder
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.AmanDarkSurfaceElevated

data class ActionOption(
    val type: ActionType,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val defaultSummary: String
)

val availableActions = listOf(
    ActionOption(
        ActionType.SPEAK_TEXT,
        "Speak Text (TTS)",
        "Speech & Audio",
        "Converts written text to spoken voice aloud",
        Icons.Default.RecordVoiceOver,
        "Speak custom text"
    ),
    ActionOption(
        ActionType.SHOW_NOTIFICATION,
        "Show Notification",
        "Alerts & UI",
        "Displays a persistent or popup notification",
        Icons.Default.Notifications,
        "Show alert notification"
    ),
    ActionOption(
        ActionType.PLAY_SOUND,
        "Play Sound",
        "Speech & Audio",
        "Plays system notification or alarm beep",
        Icons.Default.MusicNote,
        "Play alert ringtone sound"
    ),
    ActionOption(
        ActionType.VIBRATE,
        "Vibrate Device",
        "Device Settings",
        "Vibrates device with short, long or double pulse",
        Icons.Default.Vibration,
        "Short pulse vibration"
    ),
    ActionOption(
        ActionType.TOGGLE_FLASHLIGHT,
        "Toggle Torch / Flashlight",
        "Device Settings",
        "Turns phone camera flashlight on, off or toggle",
        Icons.Default.FlashlightOn,
        "Toggle Flashlight on/off"
    ),
    ActionOption(
        ActionType.SET_VOLUME,
        "Set Media Volume",
        "Device Settings",
        "Adjusts media volume to a specific percentage",
        Icons.Default.VolumeUp,
        "Set media volume"
    ),
    ActionOption(
        ActionType.SHOW_TOAST,
        "Show Quick Toast",
        "Alerts & UI",
        "Displays brief on-screen toast popup",
        Icons.Default.ChatBubbleOutline,
        "Show toast popup"
    ),
    ActionOption(
        ActionType.COPY_TO_CLIPBOARD,
        "Copy to Clipboard",
        "Clipboard",
        "Copies designated text to system clipboard",
        Icons.Default.ContentCopy,
        "Copy text to clipboard"
    ),
    ActionOption(
        ActionType.OPEN_URL,
        "Open Website URL",
        "Apps & Links",
        "Opens designated link in default browser",
        Icons.Default.Language,
        "Open website URL"
    ),
    ActionOption(
        ActionType.WAIT_DELAY,
        "Wait / Delay",
        "Flow Control",
        "Pauses automation execution for seconds",
        Icons.Default.HourglassEmpty,
        "Wait 3 seconds"
    ),
    ActionOption(
        ActionType.LOG_MESSAGE,
        "Write to System Log",
        "Flow Control",
        "Writes an audit entry to the Micro Aman system log",
        Icons.Default.Subject,
        "Log message"
    ),
    ActionOption(
        ActionType.WHATSAPP_AUTO_REPLY,
        "WhatsApp Auto Reply",
        "Social & Messaging",
        "Automatically sends a reply message to WhatsApp sender",
        Icons.Default.Chat,
        "WhatsApp auto reply"
    ),
    ActionOption(
        ActionType.SET_RINGER_MODE,
        "Set Ringer Mode (Silent / Vibrate / Normal)",
        "Device Settings",
        "Switch phone to Silent (Mute), Vibrate, or Normal sound mode",
        Icons.Default.VolumeOff,
        "Set phone to Silent mode"
    ),
    ActionOption(
        ActionType.SEND_SMS,
        "Send SMS / Auto-Reply",
        "Social & Messaging",
        "Automatically sends an SMS text reply in situations (Driving, Charging, Meeting)",
        Icons.Default.Send,
        "Send SMS auto reply"
    )
)

@Composable
fun AddActionDialog(
    onDismiss: () -> Unit,
    onActionSelected: (MacroAction) -> Unit
) {
    var selectedOption by remember { mutableStateOf<ActionOption?>(null) }

    // Config states
    var ttsText by remember { mutableStateOf("Micro Aman automation activated!") }
    var notificationTitle by remember { mutableStateOf("Micro Aman Alert") }
    var notificationMessage by remember { mutableStateOf("Action completed successfully") }
    var toastText by remember { mutableStateOf("Micro Aman executed") }
    var volumeLevel by remember { mutableFloatStateOf(50f) }
    var delaySeconds by remember { mutableFloatStateOf(3f) }
    var urlText by remember { mutableStateOf("https://google.com") }
    var whatsappReplyText by remember {
        mutableStateOf("Namaste! I am currently busy and will get back to you soon. - Micro Aman Auto Reply")
    }
    var ringerMode by remember { mutableStateOf("SILENT") } // "SILENT", "VIBRATE", "NORMAL"
    var smsReplyText by remember {
        mutableStateOf("Namaste! Main abhi busy hoon, baad mein call karta hoon. - Micro Aman Auto Reply")
    }
    var smsTargetNumber by remember { mutableStateOf("") }

    if (selectedOption == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ActionBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Action (Blue)", color = ActionBlue, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableActions) { option ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (option.type) {
                                        ActionType.SPEAK_TEXT,
                                        ActionType.SHOW_NOTIFICATION,
                                        ActionType.SHOW_TOAST,
                                        ActionType.SET_VOLUME,
                                        ActionType.WAIT_DELAY,
                                        ActionType.OPEN_URL,
                                        ActionType.WHATSAPP_AUTO_REPLY -> selectedOption = option
                                        else -> {
                                            onActionSelected(
                                                MacroAction(
                                                    type = option.type,
                                                    summary = option.defaultSummary
                                                )
                                            )
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = AmanDarkSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmanDarkBorder))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ActionBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = option.title,
                                        tint = ActionBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = option.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = AmanDarkSurface
        )
    } else {
        // Detailed configuration dialog for the chosen action
        AlertDialog(
            onDismissRequest = { selectedOption = null },
            title = { Text("Configure ${selectedOption?.title}", color = ActionBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    when (selectedOption?.type) {
                        ActionType.SPEAK_TEXT -> {
                            Text("Text to Speak Aloud (TTS):", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = ttsText,
                                onValueChange = { ttsText = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActionBlue,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        ActionType.SHOW_NOTIFICATION -> {
                            Text("Notification Title:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            OutlinedTextField(
                                value = notificationTitle,
                                onValueChange = { notificationTitle = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActionBlue,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Notification Message:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            OutlinedTextField(
                                value = notificationMessage,
                                onValueChange = { notificationMessage = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActionBlue,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        ActionType.SHOW_TOAST -> {
                            Text("Toast Message:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = toastText,
                                onValueChange = { toastText = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActionBlue,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        ActionType.SET_VOLUME -> {
                            Text("Target Media Volume: ${volumeLevel.toInt()}%", color = Color.White, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Slider(
                                value = volumeLevel,
                                onValueChange = { volumeLevel = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = ActionBlue,
                                    activeTrackColor = ActionBlue,
                                    inactiveTrackColor = Color(0xFF334155)
                                )
                            )
                        }
                        ActionType.WAIT_DELAY -> {
                            Text("Delay Duration: ${delaySeconds.toInt()} seconds", color = Color.White, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Slider(
                                value = delaySeconds,
                                onValueChange = { delaySeconds = it },
                                valueRange = 1f..30f,
                                steps = 29,
                                colors = SliderDefaults.colors(
                                    thumbColor = ActionBlue,
                                    activeTrackColor = ActionBlue,
                                    inactiveTrackColor = Color(0xFF334155)
                                )
                            )
                        }
                        ActionType.OPEN_URL -> {
                            Text("Website URL to Open:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = urlText,
                                onValueChange = { urlText = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActionBlue,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        ActionType.WHATSAPP_AUTO_REPLY -> {
                            Text("Auto-Reply Message Text:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = whatsappReplyText,
                                onValueChange = { whatsappReplyText = it },
                                placeholder = { Text("Type reply message...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActionBlue,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Quick Presets (Tap to use):", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            val presets = listOf(
                                "Namaste! Main abhi busy hoon, baad mein call karta hoon.",
                                "Currently driving, will reply once I reach safely.",
                                "In an important meeting. Will get back to you soon.",
                                "Resting/DND. Will respond shortly."
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                presets.forEach { preset ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { whatsappReplyText = preset },
                                        color = AmanDarkSurfaceElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "• $preset",
                                            color = AmanCyan,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "💡 Supports tokens: {sender} and {message}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        ActionType.SET_RINGER_MODE -> {
                            Text("Select Desired Ringer Mode:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            val modes = listOf(
                                Triple("SILENT", "Silent Mode 🔕", "Completely mute ringtone and notifications (ideal when charging or sleeping)"),
                                Triple("VIBRATE", "Vibrate Only 📳", "Mute sound but vibrate for calls & alerts"),
                                Triple("NORMAL", "Normal Sound 🔔", "Sound turned on with audible ringtones")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                modes.forEach { (modeKey, modeTitle, modeDesc) ->
                                    val isSelected = (ringerMode == modeKey)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { ringerMode = modeKey },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) ActionBlue.copy(alpha = 0.15f) else AmanDarkSurfaceElevated
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) ActionBlue else AmanDarkBorder
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { ringerMode = modeKey },
                                                colors = RadioButtonDefaults.colors(selectedColor = ActionBlue)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(modeTitle, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text(modeDesc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ActionType.SEND_SMS -> {
                            Text("SMS Auto-Reply Text:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = smsReplyText,
                                onValueChange = { smsReplyText = it },
                                placeholder = { Text("Enter auto-reply text...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActionBlue,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Situation Presets (Tap to use):", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            val smsPresets = listOf(
                                "Currently driving right now. Will call or reply once I reach safely.",
                                "Namaste! Main abhi busy hoon, baad mein call karta hoon.",
                                "Phone is on charging. Will respond as soon as I pick it up.",
                                "Auto-Reply: Currently unavailable. Please call if urgent."
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                smsPresets.forEach { preset ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { smsReplyText = preset },
                                        color = AmanDarkSurfaceElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "• $preset",
                                            color = AmanCyan,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "💡 Supports tokens: {sender}, {message}, {time}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val opt = selectedOption ?: return@Button
                        val action = when (opt.type) {
                            ActionType.SPEAK_TEXT -> MacroAction(
                                type = opt.type,
                                summary = "Speak: '$ttsText'",
                                params = mapOf("text" to ttsText)
                            )
                            ActionType.SHOW_NOTIFICATION -> MacroAction(
                                type = opt.type,
                                summary = "Notify: $notificationTitle",
                                params = mapOf("title" to notificationTitle, "message" to notificationMessage)
                            )
                            ActionType.SHOW_TOAST -> MacroAction(
                                type = opt.type,
                                summary = "Toast: '$toastText'",
                                params = mapOf("message" to toastText)
                            )
                            ActionType.SET_VOLUME -> MacroAction(
                                type = opt.type,
                                summary = "Set Volume to ${volumeLevel.toInt()}%",
                                params = mapOf("volume" to volumeLevel.toInt().toString())
                            )
                            ActionType.WAIT_DELAY -> MacroAction(
                                type = opt.type,
                                summary = "Wait ${delaySeconds.toInt()} seconds",
                                params = mapOf("seconds" to delaySeconds.toInt().toString())
                            )
                            ActionType.OPEN_URL -> MacroAction(
                                type = opt.type,
                                summary = "Open URL: $urlText",
                                params = mapOf("url" to urlText)
                            )
                            ActionType.WHATSAPP_AUTO_REPLY -> MacroAction(
                                type = opt.type,
                                summary = "WhatsApp Auto-Reply: \"${whatsappReplyText.take(25)}${if (whatsappReplyText.length > 25) "..." else ""}\"",
                                params = mapOf("reply_text" to whatsappReplyText)
                            )
                            ActionType.SET_RINGER_MODE -> MacroAction(
                                type = opt.type,
                                summary = when (ringerMode) {
                                    "SILENT" -> "Set Phone to Silent Mode 🔕"
                                    "VIBRATE" -> "Set Phone to Vibrate Mode 📳"
                                    else -> "Set Phone to Normal Sound 🔔"
                                },
                                params = mapOf("mode" to ringerMode)
                            )
                            ActionType.SEND_SMS -> MacroAction(
                                type = opt.type,
                                summary = "SMS Auto-Reply: \"${smsReplyText.take(25)}${if (smsReplyText.length > 25) "..." else ""}\"",
                                params = mapOf("reply_text" to smsReplyText, "target_contact" to smsTargetNumber.trim())
                            )
                            else -> MacroAction(type = opt.type, summary = opt.defaultSummary)
                        }
                        onActionSelected(action)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                ) {
                    Text("Add Action", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOption = null }) {
                    Text("Back", color = Color(0xFF94A3B8))
                }
            },
            containerColor = AmanDarkSurface
        )
    }
}
