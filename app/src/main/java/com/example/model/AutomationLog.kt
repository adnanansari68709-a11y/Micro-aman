package com.example.model

data class AutomationLog(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val macroName: String,
    val eventType: String, // TRIGGER, ACTION, CONSTRAINT, SYSTEM, ERROR
    val detail: String,
    val status: String // SUCCESS, FAILED, INFO, SKIPPED
)
