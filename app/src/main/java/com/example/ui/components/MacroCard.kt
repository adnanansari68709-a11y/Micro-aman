package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Macro
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.ActionBlueContainer
import com.example.ui.theme.AmanCyan
import com.example.ui.theme.AmanDarkBorder
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.ConstraintGreen
import com.example.ui.theme.ConstraintGreenContainer
import com.example.ui.theme.TriggerRed
import com.example.ui.theme.TriggerRedContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MacroCard(
    macro: Macro,
    onToggle: (Boolean) -> Unit,
    onTest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("macro_card_${macro.id}"),
        colors = CardDefaults.cardColors(
            containerColor = AmanDarkSurface
        ),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (macro.enabled) AmanDarkBorder else AmanDarkBorder.copy(alpha = 0.4f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category tag + Title + Enabled Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Accent Indicator Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (macro.enabled) AmanCyan else Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = macro.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (macro.enabled) Color.White else Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = macro.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = AmanCyan
                            )
                            if (macro.triggerCount > 0) {
                                Text(
                                    text = " • Fired ${macro.triggerCount}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Switch
                Switch(
                    checked = macro.enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("macro_switch_${macro.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AmanCyan,
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }

            if (macro.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = macro.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges for Triggers (Red), Actions (Blue), Constraints (Green)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Triggers Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TriggerRedContainer)
                        .border(1.dp, TriggerRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Trigger",
                            tint = TriggerRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${macro.triggers.size} Trigger${if (macro.triggers.size > 1) "s" else ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TriggerRed
                        )
                    }
                }

                // Actions Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ActionBlueContainer)
                        .border(1.dp, ActionBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Action",
                            tint = ActionBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${macro.actions.size} Action${if (macro.actions.size > 1) "s" else ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ActionBlue
                        )
                    }
                }

                // Constraints Badge
                if (macro.constraints.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ConstraintGreenContainer)
                            .border(1.dp, ConstraintGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Constraint",
                                tint = ConstraintGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${macro.constraints.size} Constraint${if (macro.constraints.size > 1) "s" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ConstraintGreen
                            )
                        }
                    }
                }
            }

            // Expanded view details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Trigger summaries
                    Text("Triggers:", style = MaterialTheme.typography.labelMedium, color = TriggerRed)
                    macro.triggers.forEach { t ->
                        Text(" • ${t.summary}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Actions:", style = MaterialTheme.typography.labelMedium, color = ActionBlue)
                    macro.actions.forEach { a ->
                        Text(" • ${a.summary}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                    }

                    if (macro.constraints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Constraints:", style = MaterialTheme.typography.labelMedium, color = ConstraintGreen)
                        macro.constraints.forEach { c ->
                            Text(" • ${c.summary}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                        }
                    }

                    if (macro.lastTriggeredAt > 0L) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val formattedTime = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(macro.lastTriggeredAt))
                        Text("Last executed: $formattedTime", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Run Test, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Button (Crucial feature in MacroDroid)
                FilledTonalIconButton(
                    onClick = onTest,
                    modifier = Modifier.testTag("test_macro_${macro.id}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ActionBlue.copy(alpha = 0.15f),
                        contentColor = ActionBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test Actions"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_macro_${macro.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Macro",
                        tint = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_macro_${macro.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Macro",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
