package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Macro

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val enabled: Boolean,
    val macroJson: String,
    val lastTriggeredAt: Long,
    val triggerCount: Int,
    val colorTag: String
) {
    fun toMacro(): Macro {
        return try {
            Macro.fromJson(macroJson)
        } catch (e: Exception) {
            Macro(
                id = id,
                name = name,
                description = description,
                category = category,
                enabled = enabled,
                lastTriggeredAt = lastTriggeredAt,
                triggerCount = triggerCount,
                colorTag = colorTag
            )
        }
    }

    companion object {
        fun fromMacro(macro: Macro): MacroEntity {
            return MacroEntity(
                id = macro.id,
                name = macro.name,
                description = macro.description,
                category = macro.category,
                enabled = macro.enabled,
                macroJson = macro.toJson(),
                lastTriggeredAt = macro.lastTriggeredAt,
                triggerCount = macro.triggerCount,
                colorTag = macro.colorTag
            )
        }
    }
}
