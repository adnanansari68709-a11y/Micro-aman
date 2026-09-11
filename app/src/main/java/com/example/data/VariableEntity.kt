package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Variable

@Entity(tableName = "variables")
data class VariableEntity(
    @PrimaryKey val name: String,
    val type: String,
    val value: String,
    val updatedAt: Long
) {
    fun toVariable() = Variable(name, type, value, updatedAt)

    companion object {
        fun fromVariable(v: Variable) = VariableEntity(v.name, v.type, v.value, v.updatedAt)
    }
}
