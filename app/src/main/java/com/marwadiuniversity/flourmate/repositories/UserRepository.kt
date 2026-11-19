package com.marwadiuniversity.flourmate.repositories

import com.marwadiuniversity.flourmate.database.UserDao
import com.marwadiuniversity.flourmate.models.User
import com.marwadiuniversity.flourmate.network.FirebaseSyncManager

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Long {
        val userId = userDao.insertUser(user)
        val fullUser = user.copy(id = userId)
        FirebaseSyncManager.syncPublishUser(fullUser)
        return userId
    }

    suspend fun loginUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email)
        if (user != null && user.passwordHash == password) {
            return user
        }
        return null
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        FirebaseSyncManager.syncPublishUser(user)
    }
}