package com.example.data.repository

import com.example.data.dao.PermittedAppDao
import com.example.data.dao.ReadingSessionDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.PermittedApp
import com.example.data.model.ReadingSession
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

class FocusRepository(
    private val readingSessionDao: ReadingSessionDao,
    private val permittedAppDao: PermittedAppDao,
    private val userSettingsDao: UserSettingsDao
) {
    // Sessions
    val allSessions: Flow<List<ReadingSession>> = readingSessionDao.getAllSessions()
    val totalFocusMinutes: Flow<Int?> = readingSessionDao.getTotalFocusMinutes()
    val completedCount: Flow<Int> = readingSessionDao.getCompletedSessionsCount()

    suspend fun saveSession(session: ReadingSession): Long = readingSessionDao.insertSession(session)
    suspend fun deleteSession(id: Int) = readingSessionDao.deleteSessionById(id)

    // Permitted Apps
    val allApps: Flow<List<PermittedApp>> = permittedAppDao.getAllApps()
    val permittedApps: Flow<List<PermittedApp>> = permittedAppDao.getPermittedApps()

    suspend fun isAppPermitted(packageName: String): Boolean? = permittedAppDao.isAppPermitted(packageName)
    suspend fun setAppPermitted(packageName: String, isPermitted: Boolean) =
        permittedAppDao.updateAppPermission(packageName, isPermitted)
    suspend fun insertApps(apps: List<PermittedApp>) = permittedAppDao.insertApps(apps)
    suspend fun setAllAppsPermitted(isPermitted: Boolean) = permittedAppDao.updateAllPermissions(isPermitted)
    suspend fun getAppCount(): Int = permittedAppDao.getAppCount()

    // Settings
    val settingsFlow: Flow<UserSettings?> = userSettingsDao.getSettingsFlow()

    suspend fun getSettings(): UserSettings {
        return userSettingsDao.getSettings() ?: UserSettings().also {
            userSettingsDao.insertOrUpdateSettings(it)
        }
    }

    suspend fun updateSettings(settings: UserSettings) = userSettingsDao.insertOrUpdateSettings(settings)
    suspend fun updateGuardianPassword(password: String) = userSettingsDao.updateGuardianPassword(password)
    suspend fun updateRingtone(ringtone: String) = userSettingsDao.updateRingtone(ringtone)
    suspend fun updateSpotifyUri(uri: String) = userSettingsDao.updateSpotifyUri(uri)
}
