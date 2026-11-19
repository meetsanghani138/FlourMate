package com.marwadiuniversity.flourmate.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "FlourMatePrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_PHONE = "userPhone"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_MILL_ID = "millId"
        private const val KEY_MILL_NAME = "millName"
        private const val KEY_USER_ADDRESS = "userAddress"
        private const val KEY_IS_ONLINE = "isOnline"
    }

    fun saveUserSession(
        userId: Long,
        name: String,
        email: String,
        phone: String,
        role: String,
        millId: Long = 0,
        millName: String = "",
        address: String = "123 Gandhi Road, Rajkot"
    ) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_PHONE, phone)
        editor.putString(KEY_USER_ROLE, role)
        editor.putLong(KEY_MILL_ID, millId)
        editor.putString(KEY_MILL_NAME, millName)
        editor.putString(KEY_USER_ADDRESS, address)
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, 0)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "User") ?: "User"

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, "") ?: ""

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "CUSTOMER") ?: "CUSTOMER"

    fun getMillId(): Long = prefs.getLong(KEY_MILL_ID, 0)

    fun setMillId(millId: Long) {
        prefs.edit().putLong(KEY_MILL_ID, millId).apply()
    }

    fun getMillName(): String = prefs.getString(KEY_MILL_NAME, "Flour Mill") ?: "Flour Mill"

    fun setMillName(millName: String) {
        prefs.edit().putString(KEY_MILL_NAME, millName).apply()
    }

    fun getUserAddress(): String = prefs.getString(KEY_USER_ADDRESS, "123 Gandhi Road, Rajkot") ?: "123 Gandhi Road, Rajkot"

    fun setUserAddress(address: String) {
        prefs.edit().putString(KEY_USER_ADDRESS, address).apply()
    }

    fun isDeliveryOnline(): Boolean = prefs.getBoolean(KEY_IS_ONLINE, true)

    fun setDeliveryOnline(online: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ONLINE, online).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
