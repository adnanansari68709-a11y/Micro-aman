package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AutomationLog

@Entity(tableName = "system_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val macroName: String,
    val eventType: String,
    val detail: String,
    val status: String
) {
    fun toLog() = AutomationLog(
        id = id,
        timestamp = timestamp,
        macroName = macroName,
        eventType = eventType,
        detail = detail,
        status = status
    )

    companion object {
        fun fromLog(log: AutomationLog) = LogEntity(
            id = log.id,
            timestamp = log.timestamp,
            macroName = log.macroName,
            eventType = log.eventType,
            detail = log.detail,
            status = log.status
        )
    }
}
