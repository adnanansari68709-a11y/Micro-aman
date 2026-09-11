package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 300")
    fun getRecentLogs(): Flow<List<LogEntity>>

    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC")
    suspend fun getAllLogsOnce(): List<LogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()
}
