package com.example

import android.app.Application
import com.example.data.repository.CollegeRepository
import com.example.data.session.SessionManager

class CollegeERPApplication : Application() {
    lateinit var sessionManager: SessionManager
        private set

    lateinit var repository: CollegeRepository
        private set

    lateinit var auditLogger: com.example.util.AuditLogger
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        repository = CollegeRepository(this, sessionManager)
        auditLogger = com.example.util.AuditLogger(repository)
    }
}
