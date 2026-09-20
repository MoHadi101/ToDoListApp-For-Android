package com.hadi.todolistapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("userId", userId).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("userId", -1)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}