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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.ConstraintType
import com.example.model.MacroConstraint
import com.example.ui.theme.AmanDarkBorder
import com.example.ui.theme.AmanDarkSurface
import com.example.ui.theme.AmanDarkSurfaceElevated
import com.example.ui.theme.ConstraintGreen

data class ConstraintOption(
    val type: ConstraintType,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val defaultSummary: String
)

val availableConstraints = listOf(
    ConstraintOption(
        ConstraintType.POWER_STATE,
        "Power State (Charging/Battery)",
        "Battery",
        "Only allow macro to run if phone is charging or on battery",
        Icons.Default.Power,
        "Device is charging"
    ),
    ConstraintOption(
        ConstraintType.SCREEN_STATE,
        "Screen State (On/Off)",
        "Device",
        "Only allow macro to run when screen is turned on or off",
        Icons.Default.ScreenLockPortrait,
        "Screen is turned ON"
    ),
    ConstraintOption(
        ConstraintType.WIFI_STATE,
        "Wi-Fi Connection",
        "Connectivity",
        "Only allow macro to run if device has active Wi-Fi",
        Icons.Default.Wifi,
        "Connected to Wi-Fi"
    ),
    ConstraintOption(
        ConstraintType.BATTERY_LEVEL_RANGE,
        "Battery Range",
        "Battery",
        "Only run if battery level is above 20%",
        Icons.Default.BatteryChargingFull,
        "Battery level > 20%"
    )
)

@Composable
fun AddConstraintDialog(
    onDismiss: () -> Unit,
    onConstraintSelected: (MacroConstraint) -> Unit
) {
    var selectedOption by remember { mutableStateOf<ConstraintOption?>(null) }
    var isChargingOption by remember { mutableStateOf(true) }

    if (selectedOption == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ConstraintGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Constraint (Green)", color = ConstraintGreen, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableConstraints) { option ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (option.type == ConstraintType.POWER_STATE) {
                                        selectedOption = option
                                    } else {
                                        onConstraintSelected(
                                            MacroConstraint(
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
                                        .background(ConstraintGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = option.title,
                                        tint = ConstraintGreen,
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
        // Detailed configuration for Power state
        AlertDialog(
            onDismissRequest = { selectedOption = null },
            title = { Text("Power State Condition", color = ConstraintGreen, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChargingOption = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isChargingOption,
                            onClick = { isChargingOption = true },
                            colors = RadioButtonDefaults.colors(selectedColor = ConstraintGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Only when Device is Charging (AC/USB)", color = Color.White)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChargingOption = false }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isChargingOption,
                            onClick = { isChargingOption = false },
                            colors = RadioButtonDefaults.colors(selectedColor = ConstraintGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Only when On Battery (Not Charging)", color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val summary = if (isChargingOption) "Only while charging" else "Only on battery"
                        onConstraintSelected(
                            MacroConstraint(
                                type = ConstraintType.POWER_STATE,
                                summary = summary,
                                params = mapOf("charging" to isChargingOption.toString())
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstraintGreen)
                ) {
                    Text("Add Constraint", color = Color.White)
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
