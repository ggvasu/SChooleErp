package com.example.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("CollegeERP_Prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_PERMISSIONS = "permissions"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SCRIPT_URL = "script_url"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_JWT_TOKEN = "jwt_token"
    }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var jwtToken: String
        get() = prefs.getString(KEY_JWT_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_JWT_TOKEN, value).apply()

    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var role: String
        get() = prefs.getString(KEY_ROLE, "Student") ?: "Student"
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    var displayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DISPLAY_NAME, value).apply()

    var permissions: String
        get() = prefs.getString(KEY_PERMISSIONS, "View") ?: "View"
        set(value) = prefs.edit().putString(KEY_PERMISSIONS, value).apply()

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()

    var scriptUrl: String
        get() = prefs.getString(KEY_SCRIPT_URL, "https://script.google.com/macros/s/AKfycbyxvJ02VKEpoXf6kKA65xnTQN1B_D8x4yhGwYo18y6tf51AI55FerLQH-yXheUesvxJ8Q/exec") ?: "https://script.google.com/macros/s/AKfycbyxvJ02VKEpoXf6kKA65xnTQN1B_D8x4yhGwYo18y6tf51AI55FerLQH-yXheUesvxJ8Q/exec"
        set(value) = prefs.edit().putString(KEY_SCRIPT_URL, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    fun hasPermission(action: String): Boolean {
        if (role == "Admin" && username == "admin") return true // Super admin always has all permissions
        val userPerms = permissions.split(",")
        return userPerms.contains(action) || userPerms.contains("All")
    }

    fun logout() {
        val savedUrl = scriptUrl
        val savedDark = isDarkMode
        prefs.edit().clear().apply()
        // Retain infrastructure configurations
        scriptUrl = savedUrl
        isDarkMode = savedDark
        isLoggedIn = false
    }
}
