package com.marwadiuniversity.flourmate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.marwadiuniversity.flourmate.models.Mill

@Dao
interface MillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMill(mill: Mill): Long

    @Query("SELECT * FROM mills WHERE id = :millId LIMIT 1")
    suspend fun getMillById(millId: Long): Mill?

    @Query("SELECT * FROM mills WHERE ownerId = :ownerId LIMIT 1")
    suspend fun getMillByOwnerId(ownerId: Long): Mill?

    @Query("SELECT * FROM mills")
    suspend fun getAllMills(): List<Mill>

    @Update
    suspend fun updateMill(mill: Mill)
}
