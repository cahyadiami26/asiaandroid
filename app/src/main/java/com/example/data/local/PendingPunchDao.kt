package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPunchDao {
    @Query("SELECT * FROM pending_punches ORDER BY id ASC")
    fun getAllPunches(): Flow<List<PendingPunch>>

    @Query("SELECT * FROM pending_punches WHERE status = 'QUEUED' ORDER BY id ASC")
    suspend fun getQueuedPunches(): List<PendingPunch>

    @Query("SELECT COUNT(*) FROM pending_punches WHERE status = 'QUEUED'")
    fun getQueuedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPunch(punch: PendingPunch): Long

    @Update
    suspend fun updatePunch(punch: PendingPunch)

    @Delete
    suspend fun deletePunch(punch: PendingPunch)

    @Query("DELETE FROM pending_punches WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pending_punches")
    suspend fun clearAll()
}
