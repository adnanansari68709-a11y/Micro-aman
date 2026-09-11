package com.example.ui.components

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MacroTrigger
import com.example.model.TriggerType
import com.example.ui.theme.AmanCyan
import com.example.ui.theme.AmanDarkBorder
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.AmanDarkSurfaceElevated
import com.example.ui.theme.ConstraintGreen
import com.example.ui.theme.TriggerRed

data class TriggerOption(
    val type: TriggerType,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val defaultSummary: String
)

val availableTriggers = listOf(
    TriggerOption(
        TriggerType.BATTERY_LEVEL,
        "Battery Level",
        "Battery & Power",
        "Triggers when battery reaches or drops to a specific %",
        Icons.Default.BatteryChargingFull,
        "Battery Level >= 100%"
    ),
    TriggerOption(
        TriggerType.POWER_CONNECTED,
        "Charger Connected",
        "Battery & Power",
        "Triggers when AC or USB power cable is plugged in",
        Icons.Default.BatteryChargingFull,
        "Power Connected"
    ),
    TriggerOption(
        TriggerType.POWER_DISCONNECTED,
        "Charger Disconnected",
        "Battery & Power",
        "Triggers when phone is unplugged from charging",
        Icons.Default.BatteryChargingFull,
        "Power Disconnected"
    ),
    TriggerOption(
        TriggerType.SHAKE_DEVICE,
        "Shake Device",
        "Sensors & Motion",
        "Triggers by shaking phone vigorously (Accelerometer)",
        Icons.Default.Vibration,
        "Shake Phone (Accelerometer)"
    ),
    TriggerOption(
        TriggerType.FLIP_DEVICE,
        "Flip Phone (Face Down)",
        "Sensors & Motion",
        "Triggers when device is placed face-down on a surface",
        Icons.Default.ScreenRotation,
        "Phone placed face down"
    ),
    TriggerOption(
        TriggerType.SCREEN_ON,
        "Screen Turned On",
        "Device Screen",
        "Triggers whenever phone screen turns on",
        Icons.Default.ScreenLockPortrait,
        "Screen turned ON"
    ),
    TriggerOption(
        TriggerType.SCREEN_OFF,
        "Screen Turned Off",
        "Device Screen",
        "Triggers whenever phone screen turns off",
        Icons.Default.ScreenLockPortrait,
        "Screen turned OFF"
    ),
    TriggerOption(
        TriggerType.DEVICE_UNLOCKED,
        "Device Unlocked",
        "Device Screen",
        "Triggers when user unlocks device security screen",
        Icons.Default.LockOpen,
        "Device unlocked by user"
    ),
    TriggerOption(
        TriggerType.HEADPHONES_PLUGGED,
        "Headphones Plugged In",
        "Device Hardware",
        "Triggers when 3.5mm jack or USB-C headset is plugged in",
        Icons.Default.Headphones,
        "Headphones plugged in"
    ),
    TriggerOption(
        TriggerType.HEADPHONES_UNPLUGGED,
        "Headphones Unplugged",
        "Device Hardware",
        "Triggers when headphones are detached",
        Icons.Default.Headphones,
        "Headphones unplugged"
    ),
    TriggerOption(
        TriggerType.WIFI_CONNECTED,
        "Wi-Fi Connected",
        "Connectivity",
        "Triggers when Wi-Fi connects to a network",
        Icons.Default.Wifi,
        "Connected to Wi-Fi"
    ),
    TriggerOption(
        TriggerType.TIME_OF_DAY,
        "Regular Time of Day",
        "Date & Time",
        "Triggers daily at a specific hour and minute",
        Icons.Default.AccessTime,
        "Time of day (08:00 AM)"
    ),
    TriggerOption(
        TriggerType.WHATSAPP_MESSAGE_RECEIVED,
        "WhatsApp Message Received",
        "Social & Messaging",
        "Triggers when any contact or a specific number sends a WhatsApp message",
        Icons.Default.Chat,
        "WhatsApp message received"
    ),
    TriggerOption(
        TriggerType.SMS_RECEIVED,
        "SMS Message Received",
        "Social & Messaging",
        "Triggers when an incoming SMS text is received (all or specific contact)",
        Icons.Default.Chat,
        "SMS message received"
    )
)

@Composable
fun AddTriggerDialog(
    onDismiss: () -> Unit,
    onTriggerSelected: (MacroTrigger) -> Unit
) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf<TriggerOption?>(null) }
    var batteryLevel by remember { mutableFloatStateOf(100f) }

    // WhatsApp configuration state
    var whatsappMatchType by remember { mutableStateOf("ALL") } // "ALL" or "SPECIFIC"
    var targetContactOrNumber by remember { mutableStateOf("") }
    var whatsappKeyword by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            try {
                val cursor = context.contentResolver.query(it, arrayOf(ContactsContract.Contacts.DISPLAY_NAME), null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val name = c.getString(0)
                        if (!name.isNullOrBlank()) {
                            targetContactOrNumber = name
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (selectedOption == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TriggerRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Trigger (Red)", color = TriggerRed, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTriggers) { option ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (option.type == TriggerType.BATTERY_LEVEL ||
                                        option.type == TriggerType.WHATSAPP_MESSAGE_RECEIVED ||
                                        option.type == TriggerType.SMS_RECEIVED) {
                                        selectedOption = option
                                    } else {
                                        onTriggerSelected(
                                            MacroTrigger(
                                                type = option.type,
                                                summary = option.defaultSummary
                                            )
                                        )
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
                                        .background(TriggerRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = option.title,
                                        tint = TriggerRed,
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
    } else if (selectedOption?.type == TriggerType.WHATSAPP_MESSAGE_RECEIVED || selectedOption?.type == TriggerType.SMS_RECEIVED) {
        val isSms = selectedOption?.type == TriggerType.SMS_RECEIVED
        val channelName = if (isSms) "SMS" else "WhatsApp"
        // WhatsApp/SMS Trigger Configuration Dialog
        AlertDialog(
            onDismissRequest = { selectedOption = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = TriggerRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$channelName Trigger Setup", color = TriggerRed, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Choose which incoming $channelName messages should fire this macro:",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Option 1: Any Sender
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { whatsappMatchType = "ALL" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (whatsappMatchType == "ALL") TriggerRed.copy(alpha = 0.15f) else AmanDarkSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (whatsappMatchType == "ALL") TriggerRed else AmanDarkBorder
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
                                selected = (whatsappMatchType == "ALL"),
                                onClick = { whatsappMatchType = "ALL" },
                                colors = RadioButtonDefaults.colors(selectedColor = TriggerRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("All WhatsApp Messages", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Reply or trigger for any contact or group message", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    // Option 2: Specific Contact or Number
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { whatsappMatchType = "SPECIFIC" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (whatsappMatchType == "SPECIFIC") TriggerRed.copy(alpha = 0.15f) else AmanDarkSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (whatsappMatchType == "SPECIFIC") TriggerRed else AmanDarkBorder
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
                                selected = (whatsappMatchType == "SPECIFIC"),
                                onClick = { whatsappMatchType = "SPECIFIC" },
                                colors = RadioButtonDefaults.colors(selectedColor = TriggerRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Specific Number or Contact", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Trigger only when a specific phone number or contact texts", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    if (whatsappMatchType == "SPECIFIC") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = targetContactOrNumber,
                                onValueChange = { targetContactOrNumber = it },
                                label = { Text("Phone Number or Contact Name") },
                                placeholder = { Text("e.g. +91 9876543210 or Ramesh") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TriggerRed,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedButton(
                                onClick = { contactPickerLauncher.launch(null) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmanCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmanCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pick from Device Contacts", fontSize = 12.sp)
                            }
                        }
                    }

                    // Keyword filter (Optional)
                    OutlinedTextField(
                        value = whatsappKeyword,
                        onValueChange = { whatsappKeyword = it },
                        label = { Text("Keyword Filter (Optional)") },
                        placeholder = { Text("e.g. urgent, hello (blank for any text)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TriggerRed,
                            unfocusedBorderColor = AmanDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val channel = if (isSms) "SMS" else "WhatsApp"
                        val summary = if (whatsappMatchType == "ALL") {
                            if (whatsappKeyword.isNotBlank()) "$channel message (contains \"$whatsappKeyword\")"
                            else "Any $channel message received"
                        } else {
                            val contactName = if (targetContactOrNumber.isNotBlank()) targetContactOrNumber else "Specific Contact"
                            if (whatsappKeyword.isNotBlank()) "$channel from $contactName (contains \"$whatsappKeyword\")"
                            else "$channel message from $contactName"
                        }

                        onTriggerSelected(
                            MacroTrigger(
                                type = if (isSms) TriggerType.SMS_RECEIVED else TriggerType.WHATSAPP_MESSAGE_RECEIVED,
                                summary = summary,
                                params = mapOf(
                                    "match_type" to whatsappMatchType,
                                    "target_contact" to targetContactOrNumber.trim(),
                                    "keyword" to whatsappKeyword.trim()
                                )
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TriggerRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add Trigger", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOption = null }) {
                    Text("Back", color = Color(0xFF94A3B8))
                }
            },
            containerColor = AmanDarkSurface
        )
    } else {
        // Battery % configuration
        AlertDialog(
            onDismissRequest = { selectedOption = null },
            title = { Text("Configure Battery Level", color = TriggerRed) },
            text = {
                Column {
                    Text(
                        "Trigger when battery reaches: ${batteryLevel.toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = batteryLevel,
                        onValueChange = { batteryLevel = it },
                        valueRange = 1f..100f,
                        steps = 99,
                        colors = SliderDefaults.colors(
                            thumbColor = TriggerRed,
                            activeTrackColor = TriggerRed,
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val level = batteryLevel.toInt()
                        onTriggerSelected(
                            MacroTrigger(
                                type = TriggerType.BATTERY_LEVEL,
                                summary = "Battery Level >= $level%",
                                params = mapOf("level" to level.toString(), "operator" to ">=")
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TriggerRed)
                ) {
                    Text("Add Trigger", color = Color.White)
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
