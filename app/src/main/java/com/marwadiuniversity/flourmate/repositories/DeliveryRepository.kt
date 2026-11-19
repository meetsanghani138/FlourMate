package com.marwadiuniversity.flourmate.repositories

import com.marwadiuniversity.flourmate.database.DeliveryDao
import com.marwadiuniversity.flourmate.models.DeliveryAssignment
import com.marwadiuniversity.flourmate.models.DeliveryLocation

class DeliveryRepository(private val deliveryDao: DeliveryDao) {

    suspend fun saveAssignment(assignment: DeliveryAssignment): Long {
        return deliveryDao.insertAssignment(assignment)
    }

    suspend fun updateAssignmentStatus(assignment: DeliveryAssignment) {
        deliveryDao.updateAssignment(assignment)
    }

    suspend fun getAssignmentsForPartner(partnerId: Long): List<DeliveryAssignment> {
        return deliveryDao.getAssignmentsForPartner(partnerId)
    }

    suspend fun updateLocation(partnerId: Long, orderId: Long, lat: Double, lng: Double): Long {
        return deliveryDao.insertLocation(
            DeliveryLocation(
                deliveryPartnerId = partnerId,
                orderId = orderId,
                latitude = lat,
                longitude = lng
            )
        )
    }

    suspend fun getLatestLocation(partnerId: Long): DeliveryLocation? {
        return deliveryDao.getLatestLocation(partnerId)
    }
}
