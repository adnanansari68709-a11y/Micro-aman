package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Macro
import com.example.model.MacroAction
import com.example.model.MacroConstraint
import com.example.model.MacroTrigger
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.ActionBlueContainer
import com.example.ui.theme.AmanCyan
import com.example.ui.theme.AmanDarkBg
import com.example.ui.theme.AmanDarkBorder
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.AmanDarkSurfaceElevated
import com.example.ui.theme.ConstraintGreen
import com.example.ui.theme.ConstraintGreenContainer
import com.example.ui.theme.TriggerRed
import com.example.ui.theme.TriggerRedContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroBuilderDialog(
    macroDraft: Macro,
    onUpdateMetadata: (name: String, description: String, category: String, colorTag: String) -> Unit,
    onAddTrigger: (MacroTrigger) -> Unit,
    onRemoveTrigger: (String) -> Unit,
    onAddAction: (MacroAction) -> Unit,
    onRemoveAction: (String) -> Unit,
    onAddConstraint: (MacroConstraint) -> Unit,
    onRemoveConstraint: (String) -> Unit,
    onTestMacro: (Macro) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAddTrigger by remember { mutableStateOf(false) }
    var showAddAction by remember { mutableStateOf(false) }
    var showAddConstraint by remember { mutableStateOf(false) }

    var macroName by remember { mutableStateOf(macroDraft.name) }
    var macroDesc by remember { mutableStateOf(macroDraft.description) }
    var macroCategory by remember { mutableStateOf(macroDraft.category) }

    val categories = listOf("Custom", "Battery", "Sensors", "Utilities", "Profiles", "General")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (macroDraft.name.isBlank()) "Create New Macro" else "Edit Macro",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                onUpdateMetadata(macroName, macroDesc, macroCategory, macroDraft.colorTag)
                                onSave()
                            },
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("save_macro_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AmanCyan)
                        ) {
                            Text("Save Macro", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AmanDarkSurface)
                )
            },
            containerColor = AmanDarkBg
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Basic Info Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmanDarkBorder))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Macro Details", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = macroName,
                                onValueChange = {
                                    macroName = it
                                    onUpdateMetadata(it, macroDesc, macroCategory, macroDraft.colorTag)
                                },
                                label = { Text("Macro Name (e.g. Battery Full Voice Alert)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("macro_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmanCyan,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedLabelColor = AmanCyan,
                                    unfocusedLabelColor = Color(0xFF94A3B8),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = macroDesc,
                                onValueChange = {
                                    macroDesc = it
                                    onUpdateMetadata(macroName, it, macroCategory, macroDraft.colorTag)
                                },
                                label = { Text("Description (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmanCyan,
                                    unfocusedBorderColor = AmanDarkBorder,
                                    focusedLabelColor = AmanCyan,
                                    unfocusedLabelColor = Color(0xFF94A3B8),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Category:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.take(4).forEach { cat ->
                                    val isSelected = macroCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AmanCyan.copy(alpha = 0.2f) else AmanDarkSurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (isSelected) AmanCyan else AmanDarkBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                macroCategory = cat
                                                onUpdateMetadata(macroName, macroDesc, cat, macroDraft.colorTag)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 12.sp,
                                            color = if (isSelected) AmanCyan else Color(0xFF94A3B8),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. TRIGGERS CARD (RED)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TriggerRed.copy(alpha = 0.5f)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TriggerRedContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = TriggerRed, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Triggers", color = TriggerRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${macroDraft.triggers.size})", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                }

                                FilledTonalButton(
                                    onClick = { showAddTrigger = true },
                                    modifier = Modifier.testTag("add_trigger_button"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = TriggerRed.copy(alpha = 0.2f),
                                        contentColor = TriggerRed
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Trigger", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (macroDraft.triggers.isEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No triggers added yet. Tap '+ Add Trigger' to configure when this macro executes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                macroDraft.triggers.forEach { trigger ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AmanDarkSurfaceElevated)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(TriggerRed)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(trigger.summary, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                Text(trigger.type.title, color = TriggerRed, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }

                                        IconButton(
                                            onClick = { onRemoveTrigger(trigger.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. ACTIONS CARD (BLUE)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ActionBlue.copy(alpha = 0.5f)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ActionBlueContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Actions", color = ActionBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${macroDraft.actions.size})", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                }

                                FilledTonalButton(
                                    onClick = { showAddAction = true },
                                    modifier = Modifier.testTag("add_action_button"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = ActionBlue.copy(alpha = 0.2f),
                                        contentColor = ActionBlue
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Action", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (macroDraft.actions.isEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No actions added yet. Tap '+ Add Action' to choose what happens (e.g. Speak TTS, Notification, Vibrate, Volume, etc.)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                macroDraft.actions.forEach { action ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AmanDarkSurfaceElevated)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(ActionBlue)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(action.summary, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                Text(action.type.title, color = ActionBlue, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }

                                        IconButton(
                                            onClick = { onRemoveAction(action.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. CONSTRAINTS CARD (GREEN)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmanDarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ConstraintGreen.copy(alpha = 0.5f)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ConstraintGreenContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = ConstraintGreen, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Constraints (Optional)", color = ConstraintGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${macroDraft.constraints.size})", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                }

                                FilledTonalButton(
                                    onClick = { showAddConstraint = true },
                                    modifier = Modifier.testTag("add_constraint_button"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = ConstraintGreen.copy(alpha = 0.2f),
                                        contentColor = ConstraintGreen
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (macroDraft.constraints.isEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Optional rules: restrict execution based on battery charging state, screen state, or Wi-Fi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                macroDraft.constraints.forEach { constraint ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AmanDarkSurfaceElevated)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(ConstraintGreen)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(constraint.summary, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                Text(constraint.type.title, color = ConstraintGreen, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }

                                        IconButton(
                                            onClick = { onRemoveConstraint(constraint.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Test Button
                item {
                    FilledTonalButton(
                        onClick = {
                            val draftWithMeta = macroDraft.copy(name = macroName, description = macroDesc, category = macroCategory)
                            onTestMacro(draftWithMeta)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("test_macro_in_builder"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ActionBlue.copy(alpha = 0.15f),
                            contentColor = ActionBlue
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Run Actions Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }

    if (showAddTrigger) {
        AddTriggerDialog(
            onDismiss = { showAddTrigger = false },
            onTriggerSelected = {
                onAddTrigger(it)
                showAddTrigger = false
            }
        )
    }

    if (showAddAction) {
        AddActionDialog(
            onDismiss = { showAddAction = false },
            onActionSelected = {
                onAddAction(it)
                showAddAction = false
            }
        )
    }

    if (showAddConstraint) {
        AddConstraintDialog(
            onDismiss = { showAddConstraint = false },
            onConstraintSelected = {
                onAddConstraint(it)
                showAddConstraint = false
            }
        )
    }
}
