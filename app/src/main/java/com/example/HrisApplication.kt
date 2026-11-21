package com.example

import android.app.Application
import com.example.data.api.ApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.SessionPreferences
import com.example.data.repo.HrisRepository

class HrisApplication : Application() {

    lateinit var sessionPreferences: SessionPreferences
        private set

    lateinit var apiClient: ApiClient
        private set

    lateinit var database: AppDatabase
        private set

    lateinit var repository: HrisRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        sessionPreferences = SessionPreferences(this)
        apiClient = ApiClient(this, sessionPreferences)
        database = AppDatabase.getInstance(this)
        repository = HrisRepository(this, apiClient, sessionPreferences, database)
    }

    companion object {
        lateinit var instance: HrisApplication
            private set
    }
}
