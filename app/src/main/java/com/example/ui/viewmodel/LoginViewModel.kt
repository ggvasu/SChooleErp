package com.example.ui.viewmodel

import android.app.Application
import android.util.Base64
import androidx.lifecycle.viewModelScope
import com.example.CollegeERPApplication
import com.example.data.models.GenericRequest
import com.example.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(application: Application) : BaseViewModel(application) {
    private val app = application as CollegeERPApplication
    private val repository = app.repository
    val sessionManager: SessionManager = app.sessionManager

    private val _loginSuccess = MutableStateFlow<Boolean>(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun handleLogin(
        username: String,
        role: String,
        pass: String,
        remember: Boolean,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.login(GenericRequest("login", username, role, pass))
                if (res.success) {
                    // Generate a standard JWT on successful authentication
                    val header = Base64.encodeToString(
                        "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".toByteArray(),
                        Base64.NO_WRAP or Base64.URL_SAFE
                    )
                    
                    // Expiry is 1 hour from now
                    val expiry = (System.currentTimeMillis() / 1000) + 3600
                    val payloadJson = """
                        {
                          "sub": "$username",
                          "role": "$role",
                          "name": "${res.studentName ?: username}",
                          "permissions": "${res.permissions ?: "View"}",
                          "exp": $expiry
                        }
                    """.trimIndent()
                    val payload = Base64.encodeToString(
                        payloadJson.toByteArray(),
                        Base64.NO_WRAP or Base64.URL_SAFE
                    )
                    val signature = Base64.encodeToString(
                        "secure_signature_key".toByteArray(),
                        Base64.NO_WRAP or Base64.URL_SAFE
                    )
                    
                    val token = "$header.$payload.$signature"
                    
                    sessionManager.isLoggedIn = true
                    sessionManager.jwtToken = token
                    sessionManager.username = res.username ?: username
                    sessionManager.role = res.role ?: role
                    sessionManager.displayName = res.studentName ?: (if (role == "Admin") "Administrator" else "Student")
                    sessionManager.permissions = res.permissions ?: "View"
                    sessionManager.rememberMe = remember

                    _loginSuccess.value = true
                    onSuccess(res.role ?: role)
                } else {
                    _statusMessage.value = res.message ?: "Authentication failed"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Login error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Helper to decode claims from current JWT token
    fun getJwtClaims(): Map<String, Any>? {
        val token = sessionManager.jwtToken
        if (token.isEmpty()) return null
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payloadString = String(Base64.decode(parts[1], Base64.DEFAULT))
                val json = JSONObject(payloadString)
                val map = mutableMapOf<String, Any>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = json.get(key)
                }
                map
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Check if the current token is expired
    fun isTokenExpired(): Boolean {
        val claims = getJwtClaims() ?: return true
        val exp = claims["exp"] as? Long ?: return true
        return (System.currentTimeMillis() / 1000) > exp
    }
}
