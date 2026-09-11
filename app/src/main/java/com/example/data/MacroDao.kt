package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY name ASC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE enabled = 1")
    suspend fun getEnabledMacrosOnce(): List<MacroEntity>

    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getMacroById(id: String): MacroEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacros(macros: List<MacroEntity>)

    @Update
    suspend fun updateMacro(macro: MacroEntity)

    @Query("UPDATE macros SET enabled = :enabled WHERE id = :id")
    suspend fun setMacroEnabled(id: String, enabled: Boolean)

    @Query("UPDATE macros SET lastTriggeredAt = :timestamp, triggerCount = triggerCount + 1 WHERE id = :id")
    suspend fun incrementTriggerCount(id: String, timestamp: Long)

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacroById(id: String)

    @Query("SELECT * FROM macros")
    suspend fun getAllMacrosOnce(): List<MacroEntity>

    @Query("SELECT COUNT(*) FROM macros")
    suspend fun getMacroCount(): Int
}
