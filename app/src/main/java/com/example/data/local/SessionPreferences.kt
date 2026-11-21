package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "hris_session")

class SessionPreferences(private val context: Context) {

    companion object {
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_EXPIRES_AT = stringPreferencesKey("expires_at")
        val KEY_EMPLOYEE_NAME = stringPreferencesKey("employee_name")
        val KEY_EMPLOYEE_CODE = stringPreferencesKey("employee_code")
        val KEY_EMPLOYEE_EMAIL = stringPreferencesKey("employee_email")
        val KEY_EMPLOYEE_ROLE = stringPreferencesKey("employee_role")
        val KEY_CAN_APPROVE = booleanPreferencesKey("can_approve")

        const val DEFAULT_SERVER_URL = "https://hris.asiaraya.co.id"
    }

    val serverUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    val employeeNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMPLOYEE_NAME]
    }

    val employeeCodeFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMPLOYEE_CODE]
    }

    val employeeEmailFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMPLOYEE_EMAIL]
    }

    val employeeRoleFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMPLOYEE_ROLE]
    }

    val canApproveFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CAN_APPROVE] ?: false
    }

    suspend fun getServerUrl(): String {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL
    }

    suspend fun setServerUrl(url: String) {
        val normalized = normalizeUrl(url)
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = normalized
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String?, expiresAt: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            if (refreshToken != null) prefs[KEY_REFRESH_TOKEN] = refreshToken
            if (expiresAt != null) prefs[KEY_EXPIRES_AT] = expiresAt
        }
    }

    suspend fun saveEmployeeInfo(name: String, code: String, email: String, role: String, canApprove: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMPLOYEE_NAME] = name
            prefs[KEY_EMPLOYEE_CODE] = code
            prefs[KEY_EMPLOYEE_EMAIL] = email
            prefs[KEY_EMPLOYEE_ROLE] = role
            prefs[KEY_CAN_APPROVE] = canApprove
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_EXPIRES_AT)
            prefs.remove(KEY_EMPLOYEE_NAME)
            prefs.remove(KEY_EMPLOYEE_CODE)
            prefs.remove(KEY_EMPLOYEE_EMAIL)
            prefs.remove(KEY_EMPLOYEE_ROLE)
            prefs.remove(KEY_CAN_APPROVE)
        }
    }

    private fun normalizeUrl(url: String): String {
        var clean = url.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        if (clean.endsWith("/")) {
            clean = clean.dropLast(1)
        }
        if (clean.endsWith("/api/v1")) {
            clean = clean.removeSuffix("/api/v1")
        }
        return clean
    }
}
