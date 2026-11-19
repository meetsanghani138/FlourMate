package com.marwadiuniversity.flourmate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.marwadiuniversity.flourmate.models.DeliveryAssignment
import com.marwadiuniversity.flourmate.models.DeliveryLocation

@Dao
interface DeliveryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: DeliveryAssignment): Long

    @Update
    suspend fun updateAssignment(assignment: DeliveryAssignment)

    @Query("SELECT * FROM delivery_assignments WHERE deliveryPartnerId = :partnerId ORDER BY timestamp DESC")
    suspend fun getAssignmentsForPartner(partnerId: Long): List<DeliveryAssignment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: DeliveryLocation): Long

    @Query("SELECT * FROM delivery_locations WHERE deliveryPartnerId = :partnerId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLocation(partnerId: Long): DeliveryLocation?
}
