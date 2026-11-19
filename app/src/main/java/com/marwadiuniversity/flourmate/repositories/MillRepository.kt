package com.marwadiuniversity.flourmate.repositories

import com.marwadiuniversity.flourmate.database.MillDao
import com.marwadiuniversity.flourmate.models.Mill
import com.marwadiuniversity.flourmate.network.FirebaseSyncManager

class MillRepository(private val millDao: MillDao) {

    suspend fun createMill(mill: Mill): Long {
        val millId = millDao.insertMill(mill)
        val fullMill = mill.copy(id = millId)
        FirebaseSyncManager.syncPublishMill(fullMill)
        return millId
    }

    suspend fun getMillById(millId: Long): Mill? {
        return millDao.getMillById(millId)
    }

    suspend fun getMillByOwnerId(ownerId: Long): Mill? {
        return millDao.getMillByOwnerId(ownerId)
    }

    suspend fun getAllMills(): List<Mill> {
        return millDao.getAllMills()
    }

    suspend fun updateMill(mill: Mill) {
        millDao.updateMill(mill)
        FirebaseSyncManager.syncPublishMill(mill)
    }
}