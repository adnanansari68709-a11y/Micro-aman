package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.ConstraintType
import com.example.model.Macro
import com.example.model.MacroAction
import com.example.model.MacroConstraint
import com.example.model.MacroTrigger
import com.example.model.TriggerType
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.AmanCyan
import com.example.ui.theme.AmanDarkBg
import com.example.ui.theme.AmanDarkBorder
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.ConstraintGreen
import com.example.ui.theme.TriggerRed
import java.util.UUID

data class TemplateItem(
    val title: String,
    val description: String,
    val category: String,
    val icon: ImageVector,
    val macro: Macro
)

val templateCatalog = listOf(
    TemplateItem(
        title = "Battery 100% Voice Alarm",
        description = "Phone 100% charge hone par madhur Hindi voice alert deta hai taaki battery surakshit rahe.",
        category = "Battery",
        icon = Icons.Default.BatteryChargingFull,
        macro = Macro(
            name = "Battery 100% Voice Alarm",
            description = "Speaks battery full notification when battery reaches 100%",
            category = "Battery",
            triggers = listOf(
                MacroTrigger(type = TriggerType.BATTERY_LEVEL, summary = "Battery Level >= 100%", params = mapOf("level" to "100", "operator" to ">="))
            ),
            actions = listOf(
                MacroAction(type = ActionType.SPEAK_TEXT, summary = "Awaaz: 'Dhyan dein! Battery 100% charge ho chuki hai...'", params = mapOf("text" to "Dhyan dein! Phone ki battery 100 pratishat poori charge ho chuki hai. Kripya charger hata dein!")),
                MacroAction(type = ActionType.SHOW_NOTIFICATION, summary = "Notify: Battery Full", params = mapOf("title" to "Micro Aman", "message" to "Battery is 100% full!"))
            ),
            constraints = listOf(
                MacroConstraint(type = ConstraintType.POWER_STATE, summary = "Only while charging", params = mapOf("charging" to "true"))
            )
        )
    ),
    TemplateItem(
        title = "Shake Phone for Flashlight",
        description = "Vigorously shake your phone to turn flashlight ON or OFF anytime in the dark.",
        category = "Sensors",
        icon = Icons.Default.Vibration,
        macro = Macro(
            name = "Shake for Flashlight",
            description = "Shake phone triggers camera LED flashlight toggle",
            category = "Sensors",
            triggers = listOf(
                MacroTrigger(type = TriggerType.SHAKE_DEVICE, summary = "Shake Device (Accelerometer)")
            ),
            actions = listOf(
                MacroAction(type = ActionType.TOGGLE_FLASHLIGHT, summary = "Toggle Flashlight", params = mapOf("mode" to "TOGGLE")),
                MacroAction(type = ActionType.VIBRATE, summary = "Short haptic buzz", params = mapOf("pattern" to "SHORT"))
            )
        )
    ),
    TemplateItem(
        title = "Auto Silent & Voice Alarm on Phone Charging",
        description = "Phone charging par lagane par automatically silent mode enable karta hai aur certified Hindi voice alert deta hai.",
        category = "Battery",
        icon = Icons.Default.VolumeOff,
        macro = Macro(
            name = "Auto Silent on Phone Charging",
            description = "Phone charger connect hote hi device ko silent kar deta hai aur alert deta hai",
            category = "Battery",
            triggers = listOf(
                MacroTrigger(type = TriggerType.POWER_CONNECTED, summary = "Charger Cable Connected")
            ),
            actions = listOf(
                MacroAction(type = ActionType.SET_RINGER_MODE, summary = "Set Phone to Silent Mode 🔕", params = mapOf("mode" to "SILENT")),
                MacroAction(type = ActionType.SPEAK_TEXT, summary = "Awaaz: 'Phone charging shuru ho gayi hai, silent mode on...'", params = mapOf("text" to "Phone charging shuru ho gayi hai. Device ko silent kar diya gaya hai.")),
                MacroAction(type = ActionType.SHOW_TOAST, summary = "Toast: 'Charging: Phone Silenced 🔕'", params = mapOf("message" to "Micro Aman: Phone on charging, Silent mode activated 🔕")),
                MacroAction(type = ActionType.SHOW_NOTIFICATION, summary = "Notify: Phone Silenced for Charging", params = mapOf("title" to "Micro Aman Auto-Silent", "message" to "Charger connected: Phone automatically silenced 🔕"))
            )
        )
    ),
    TemplateItem(
        title = "Restore Normal Sound on Charger Unplugged",
        description = "Charger hatane par phone automatically normal sound mode par switch ho jata hai aur Hindi me batata hai.",
        category = "Battery",
        icon = Icons.Default.VolumeUp,
        macro = Macro(
            name = "Restore Sound on Charger Disconnected",
            description = "Charger unplug hote hi normal sound restore karta hai",
            category = "Battery",
            triggers = listOf(
                MacroTrigger(type = TriggerType.POWER_DISCONNECTED, summary = "Charger Disconnected")
            ),
            actions = listOf(
                MacroAction(type = ActionType.SET_RINGER_MODE, summary = "Restore Normal Sound 🔔", params = mapOf("mode" to "NORMAL")),
                MacroAction(type = ActionType.SPEAK_TEXT, summary = "Awaaz: 'Charger nikal diya gaya hai, awaaz normal ho gayi hai.'", params = mapOf("text" to "Charger nikal diya gaya hai. Phone ki normal awaaz wapas chalu ho gayi hai.")),
                MacroAction(type = ActionType.SHOW_TOAST, summary = "Toast: 'Normal Sound Restored 🔔'", params = mapOf("message" to "Micro Aman: Normal sound mode restored 🔔"))
            )
        )
    ),
    TemplateItem(
        title = "Low Battery 15% SOS Warning",
        description = "Vibrates SOS pulse and speaks battery critically low warning at 15% in certified Hindi.",
        category = "Battery",
        icon = Icons.Default.BatteryChargingFull,
        macro = Macro(
            name = "Low Battery 15% Warning",
            description = "Vibrates and speaks warning when battery drops to 15%",
            category = "Battery",
            triggers = listOf(
                MacroTrigger(type = TriggerType.BATTERY_LEVEL, summary = "Battery Level <= 15%", params = mapOf("level" to "15", "operator" to "<="))
            ),
            actions = listOf(
                MacroAction(type = ActionType.SPEAK_TEXT, summary = "Awaaz: 'Savdhaan! Phone ki battery 15% bachi hai...'", params = mapOf("text" to "Savdhaan! Phone ki battery 15 pratishat bachi hai. Kripya apna phone turant charge karein.")),
                MacroAction(type = ActionType.VIBRATE, summary = "SOS double pulse vibration", params = mapOf("pattern" to "DOUBLE_PULSE"))
            ),
            constraints = listOf(
                MacroConstraint(type = ConstraintType.POWER_STATE, summary = "Only when on battery", params = mapOf("charging" to "false"))
            )
        )
    ),
    TemplateItem(
        title = "Flip Phone to Silent",
        description = "Placing device face down automatically sets volume to 0% and silences sounds.",
        category = "Sensors",
        icon = Icons.Default.ScreenRotation,
        macro = Macro(
            name = "Flip Phone to Silent",
            description = "Face down sets volume to 0%",
            category = "Sensors",
            triggers = listOf(
                MacroTrigger(type = TriggerType.FLIP_DEVICE, summary = "Phone Placed Face Down")
            ),
            actions = listOf(
                MacroAction(type = ActionType.SET_VOLUME, summary = "Set volume to 0%", params = mapOf("volume" to "0")),
                MacroAction(type = ActionType.SHOW_TOAST, summary = "Toast: 'Device Silenced'", params = mapOf("message" to "Micro Aman: Device Silenced"))
            )
        )
    ),
    TemplateItem(
        title = "Headphones Music Volume Booster",
        description = "Sets volume to 60% and speaks 'Headphones jud gaye hain' when plugged in.",
        category = "Utilities",
        icon = Icons.Default.Headphones,
        macro = Macro(
            name = "Headphones Music Booster",
            description = "Boosts volume when headphones plugged in",
            category = "Utilities",
            triggers = listOf(
                MacroTrigger(type = TriggerType.HEADPHONES_PLUGGED, summary = "Headphones Plugged In")
            ),
            actions = listOf(
                MacroAction(type = ActionType.SET_VOLUME, summary = "Set volume to 60%", params = mapOf("volume" to "60")),
                MacroAction(type = ActionType.SPEAK_TEXT, summary = "Awaaz: 'Headphones jud gaye hain...'", params = mapOf("text" to "Headphones jud gaye hain. Volume 60 pratishat kar diya gaya hai, sangeet ka maza lijiye!"))
            )
        )
    ),
    TemplateItem(
        title = "Wi-Fi Connected Welcome Toast",
        description = "Displays confirmation toast and logs event when Wi-Fi connects.",
        category = "Connectivity",
        icon = Icons.Default.Wifi,
        macro = Macro(
            name = "Wi-Fi Connected Welcome",
            description = "Welcomes user when Wi-Fi connects",
            category = "Connectivity",
            triggers = listOf(
                MacroTrigger(type = TriggerType.WIFI_CONNECTED, summary = "Connected to Wi-Fi")
            ),
            actions = listOf(
                MacroAction(type = ActionType.SHOW_TOAST, summary = "Toast: 'Wi-Fi connected'", params = mapOf("message" to "Micro Aman: Wi-Fi connected")),
                MacroAction(type = ActionType.LOG_MESSAGE, summary = "Log Wi-Fi connection", params = mapOf("message" to "Wi-Fi connected successfully"))
            )
        )
    ),
    TemplateItem(
        title = "SMS Auto-Reply: Driving & Busy Workflow",
        description = "Koi bhi incoming SMS aane par automatically customizable reply SMS bhejta hai.",
        category = "Messaging",
        icon = Icons.Default.Chat,
        macro = Macro(
            name = "SMS Auto-Reply Workflow",
            description = "Incoming SMS receive hone par auto reply bhejta hai",
            category = "Messaging",
            triggers = listOf(
                MacroTrigger(type = TriggerType.SMS_RECEIVED, summary = "Any incoming SMS message received", params = mapOf("match_type" to "ALL"))
            ),
            actions = listOf(
                MacroAction(type = ActionType.SEND_SMS, summary = "SMS Auto-Reply: \"Namaste! Main abhi busy hoon...\"", params = mapOf("reply_text" to "Namaste! Main abhi busy hoon, thodi der baad call ya message karta hoon. - Micro Aman Auto Reply")),
                MacroAction(type = ActionType.SHOW_NOTIFICATION, summary = "Notify: SMS Auto-Reply Sent", params = mapOf("title" to "Micro Aman SMS Auto-Reply", "message" to "Auto-replied to incoming SMS from {sender}"))
            )
        )
    ),
    TemplateItem(
        title = "WhatsApp Smart Auto-Reply",
        description = "WhatsApp par messages aane par automatically reply bhejta hai (all contacts ya specific number filter).",
        category = "Messaging",
        icon = Icons.Default.Chat,
        macro = Macro(
            name = "WhatsApp Smart Auto-Reply",
            description = "WhatsApp message receive hone par auto reply bhejta hai",
            category = "Messaging",
            triggers = listOf(
                MacroTrigger(type = TriggerType.WHATSAPP_MESSAGE_RECEIVED, summary = "Any WhatsApp message received", params = mapOf("match_type" to "ALL"))
            ),
            actions = listOf(
                MacroAction(type = ActionType.WHATSAPP_AUTO_REPLY, summary = "WhatsApp Auto-Reply: \"Namaste! Main abhi busy hoon...\"", params = mapOf("reply_text" to "Namaste! Main abhi busy hoon, thodi der baad call ya message karta hoon. - Micro Aman Auto Reply")),
                MacroAction(type = ActionType.SHOW_NOTIFICATION, summary = "Notify: WhatsApp Auto-Reply Sent", params = mapOf("title" to "Micro Aman Auto-Reply", "message" to "Auto-replied to WhatsApp sender"))
            )
        )
    )
)

@Composable
fun TemplatesScreen(
    onImportTemplate: (Macro) -> Unit,
    modifier: Modifier = Modifier,
    existingMacros: List<Macro> = emptyList()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmanDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "MacroDroid Templates Store",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "100% Free • Tap '+ Add' to import any popular automation instantly",
            style = MaterialTheme.typography.bodySmall,
            color = AmanCyan
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templateCatalog) { item ->
                val isAlreadyAdded = existingMacros.any {
                    it.name.trim().equals(item.title.trim(), ignoreCase = true) ||
                    it.name.trim().equals(item.macro.name.trim(), ignoreCase = true)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmanDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AmanCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = AmanCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmanCyan
                                    )
                                }
                            }

                            if (isAlreadyAdded) {
                                Surface(
                                    color = ConstraintGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ConstraintGreen.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ConstraintGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Added",
                                            color = ConstraintGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onImportTemplate(item.macro) },
                                    modifier = Modifier.testTag("import_template_${item.title.replace(" ", "_")}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmanCyan),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger and Action Pill previews
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Trigger: ${item.macro.triggers.firstOrNull()?.summary ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TriggerRed
                            )
                            Text(
                                text = "• Action: ${item.macro.actions.firstOrNull()?.type?.title ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ActionBlue
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
