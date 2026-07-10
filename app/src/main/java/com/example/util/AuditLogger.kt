package com.example.util

import android.util.Log
import com.example.data.repository.CollegeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuditLogger(private val repository: CollegeRepository) {
    private val tag = "AuditLogger"
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Logs a sensitive admin action locally and pushes it to the Google Sheets backend asynchronously.
     */
    fun logAction(user: String, action: String, details: String) {
        Log.i(tag, "AuditLog - User: $user, Action: $action, Details: $details")

        scope.launch {
            try {
                repository.logAuditRemote(user, action, details)
            } catch (e: Exception) {
                Log.e(tag, "Failed to sync audit log to Sheets backend: ${e.message}", e)
            }
        }
    }
}
