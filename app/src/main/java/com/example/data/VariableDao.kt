package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableDao {
    @Query("SELECT * FROM variables ORDER BY name ASC")
    fun getAllVariables(): Flow<List<VariableEntity>>

    @Query("SELECT * FROM variables WHERE name = :name")
    suspend fun getVariable(name: String): VariableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariable(variable: VariableEntity)

    @Query("DELETE FROM variables WHERE name = :name")
    suspend fun deleteVariable(name: String)
}
