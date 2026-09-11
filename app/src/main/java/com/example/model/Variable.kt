package com.example.model

data class Variable(
    val name: String,
    val type: String, // STRING, INTEGER, BOOLEAN
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
