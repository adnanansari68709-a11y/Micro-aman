package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Variable
import com.example.service.WhatsAppReplyManager
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.AmanCyan
import com.example.ui.theme.AmanDarkBg
import com.example.ui.theme.AmanDarkBorder
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.AmanDarkSurfaceElevated
import com.example.ui.theme.ConstraintGreen
import kotlinx.coroutines.launch

@Composable
fun VariablesAndSettingsScreen(
    variables: List<Variable>,
    onSaveVariable: (name: String, type: String, value: String) -> Unit,
    onDeleteVariable: (name: String) -> Unit,
    onTestSpeech: (String) -> Unit,
    onTestVibration: () -> Unit,
    onExportJson: suspend () -> String,
    onImportJson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAddVarDialog by remember { mutableStateOf(false) }
    var newVarName by remember { mutableStateOf("") }
    var newVarValue by remember { mutableStateOf("") }

    var testSpeechText by remember { mutableStateOf("Micro Aman automation is working perfectly!") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AmanDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Pro Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(AmanCyan, Color(0xFF7C3AED))
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmanCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmanCyan, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Micro Aman Pro",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ConstraintGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("100% FREE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ConstraintGreen)
                            }
                        }

                        Text(
                            text = "Unlimited Macros • All Triggers & Actions Unlocked • Zero Cost",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // 2. Global Variables Section
        item {
            Card(
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = AmanCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Macro Variables", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { showAddVarDialog = true },
                            modifier = Modifier.testTag("add_variable_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AmanCyan.copy(alpha = 0.15f),
                                contentColor = AmanCyan
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Variable")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (variables.isEmpty()) {
                        Text("No variables saved. Tap 'New Variable' to store text or counters.", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        variables.forEach { variable ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmanDarkSurfaceElevated)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(variable.name, color = AmanCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Value: ${variable.value} (${variable.type})", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { onDeleteVariable(variable.name) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. WhatsApp Auto-Reply & Notification Access
        item {
            val isNotificationAccessGranted = WhatsAppReplyManager.isNotificationAccessGranted(context)
            Card(
                colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isNotificationAccessGranted) ConstraintGreen else Color(0xFFF59E0B))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp Auto-Reply Access", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (isNotificationAccessGranted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ConstraintGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ConstraintGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ACTIVE", color = ConstraintGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SETUP REQUIRED", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Micro Aman monitors WhatsApp notifications and delivers instant auto-replies directly to specific numbers or all chats according to your configured macros.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNotificationAccessGranted) Color(0xFF1E293B) else Color(0xFF25D366)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isNotificationAccessGranted) "Notification Access Enabled ✓" else "Enable Notification Access for WhatsApp",
                            fontWeight = FontWeight.Bold,
                            color = if (isNotificationAccessGranted) Color(0xFF94A3B8) else Color.White
                        )
                    }
                }
            }
        }

        // 4. Hardware Test Bench
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmanDarkBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hardware Test Bench", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TTS Test
                    OutlinedTextField(
                        value = testSpeechText,
                        onValueChange = { testSpeechText = it },
                        label = { Text("TTS Voice Test String") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActionBlue,
                            unfocusedBorderColor = AmanDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTestSpeech(testSpeechText) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_speech_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Voice")
                        }

                        Button(
                            onClick = onTestVibration,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_vibrate_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Vibrate")
                        }
                    }
                }
            }
        }

        // 4. Backup, Export & Import
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmanDarkBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Backup & Share", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Export your entire automation rule set to JSON or restore from backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val json = onExportJson()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Micro Aman Backup", json))
                                    exportSuccessMessage = "All macros copied to clipboard as JSON backup!"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_macros_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AmanCyan)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showImportDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_macros_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import")
                        }
                    }

                    if (exportSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(exportSuccessMessage ?: "", color = ConstraintGreen, fontSize = 12.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Add Variable Dialog
    if (showAddVarDialog) {
        AlertDialog(
            onDismissRequest = { showAddVarDialog = false },
            title = { Text("Create Variable", color = AmanCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newVarName,
                        onValueChange = { newVarName = it },
                        label = { Text("Variable Name (e.g. user_name)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanCyan,
                            unfocusedBorderColor = AmanDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newVarValue,
                        onValueChange = { newVarValue = it },
                        label = { Text("Initial Value") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanCyan,
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
                        if (newVarName.isNotBlank()) {
                            onSaveVariable(newVarName, "STRING", newVarValue)
                            newVarName = ""
                            newVarValue = ""
                            showAddVarDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmanCyan)
                ) {
                    Text("Save", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVarDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = AmanDarkSurface
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Macros JSON", color = AmanCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Paste the JSON backup string below:", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanCyan,
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
                        onImportJson(importJsonText)
                        importJsonText = ""
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmanCyan)
                ) {
                    Text("Import Now", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = AmanDarkSurface
        )
    }
}
