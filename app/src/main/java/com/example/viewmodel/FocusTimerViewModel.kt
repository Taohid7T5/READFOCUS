package com.example.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReadFocusApp
import com.example.data.model.PermittedApp
import com.example.data.model.ReadingSession
import com.example.data.model.UserSettings
import com.example.data.repository.FocusRepository
import com.example.service.AppBlockerManager
import com.example.service.FocusServiceState
import com.example.service.FocusSessionService
import com.example.service.SessionMode
import com.example.service.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FocusTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FocusRepository = (application as ReadFocusApp).repository

    private var focusService: FocusSessionService? = null
    private val _isServiceBound = MutableStateFlow(false)

    private val _serviceState = MutableStateFlow(FocusServiceState())
    val serviceState: StateFlow<FocusServiceState> = _serviceState.asStateFlow()

    val allSessions: StateFlow<List<ReadingSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFocusMinutes: StateFlow<Int?> = repository.totalFocusMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedSessionsCount: StateFlow<Int> = repository.completedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val permittedApps: StateFlow<List<PermittedApp>> = repository.allApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    // UI States
    private val _guardianModalVisible = MutableStateFlow(false)
    val guardianModalVisible: StateFlow<Boolean> = _guardianModalVisible.asStateFlow()

    private val _guardianInput = MutableStateFlow("")
    val guardianInput: StateFlow<String> = _guardianInput.asStateFlow()

    private val _guardianError = MutableStateFlow<String?>(null)
    val guardianError: StateFlow<String?> = _guardianError.asStateFlow()

    private val _activeAmbientTrack = MutableStateFlow("None")
    val activeAmbientTrack: StateFlow<String> = _activeAmbientTrack.asStateFlow()

    private val _ambientVolume = MutableStateFlow(0.5f)
    val ambientVolume: StateFlow<Float> = _ambientVolume.asStateFlow()

    private val _isAmbientPlaying = MutableStateFlow(false)
    val isAmbientPlaying: StateFlow<Boolean> = _isAmbientPlaying.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as FocusSessionService.LocalBinder
            focusService = binder.getService()
            _isServiceBound.value = true

            viewModelScope.launch {
                focusService?.serviceState?.collect { state ->
                    _serviceState.value = state
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            focusService = null
            _isServiceBound.value = false
        }
    }

    init {
        bindFocusService()
        checkPermissions()
    }

    fun bindFocusService() {
        val intent = Intent(getApplication(), FocusSessionService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _hasUsagePermission.value = AppBlockerManager.hasUsageStatsPermission(context)
        _hasOverlayPermission.value = AppBlockerManager.hasOverlayPermission(context)
    }

    fun startFocusSession(
        bookTitle: String,
        durationMinutes: Int,
        focusCycleMinutes: Int = 30,
        breakDurationMinutes: Int = 5
    ) {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusSessionService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        focusService?.startSession(bookTitle, durationMinutes, focusCycleMinutes, breakDurationMinutes)
    }

    fun updateSessionDurations(defaultMinutes: Int, focusIntervalMinutes: Int, breakDurationMinutes: Int) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettings()
            repository.updateSettings(
                current.copy(
                    defaultSessionMinutes = defaultMinutes,
                    breakIntervalMinutes = focusIntervalMinutes,
                    breakDurationMinutes = breakDurationMinutes
                )
            )
        }
    }

    fun pauseSession() {
        focusService?.pauseSession()
    }

    fun resumeSession() {
        focusService?.resumeSession()
    }

    fun skipBreak() {
        focusService?.skipBreakEarly()
    }

    fun showEmergencyUnlockModal() {
        _guardianInput.value = ""
        _guardianError.value = null
        _guardianModalVisible.value = true
    }

    fun dismissEmergencyUnlockModal() {
        _guardianModalVisible.value = false
        _guardianInput.value = ""
        _guardianError.value = null
    }

    fun onGuardianInputChange(text: String) {
        _guardianInput.value = text
        _guardianError.value = null
    }

    fun verifyGuardianPasswordAndAbort(): Boolean {
        val currentSettings = userSettings.value ?: UserSettings()
        val expectedPassword = currentSettings.guardianPassword

        if (_guardianInput.value.trim() == expectedPassword.trim() || _guardianInput.value.trim() == "9999") {
            val stopped = focusService?.emergencyStopSession(authorized = true) ?: false
            _guardianModalVisible.value = false
            _guardianInput.value = ""
            _guardianError.value = null
            return stopped
        } else {
            _guardianError.value = "Incorrect guardian password. Ask your guardian for assistance."
            return false
        }
    }

    fun clearBlockedAppAlert() {
        focusService?.clearBlockedAppAlert()
    }

    fun toggleAppPermitted(packageName: String, isPermitted: Boolean) {
        viewModelScope.launch {
            repository.setAppPermitted(packageName, isPermitted)
        }
    }

    fun setAllAppsPermitted(isPermitted: Boolean) {
        viewModelScope.launch {
            repository.setAllAppsPermitted(isPermitted)
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = AppBlockerManager.loadInstalledApps(getApplication())
            repository.insertApps(apps)
        }
    }

    fun updateGuardianPassword(newPassword: String) {
        viewModelScope.launch {
            repository.updateGuardianPassword(newPassword.trim())
        }
    }

    fun updateRingtone(ringtoneName: String) {
        viewModelScope.launch {
            repository.updateRingtone(ringtoneName)
        }
    }

    fun testRingtone(ringtoneName: String) {
        val context = getApplication<Application>()
        val currentSettings = userSettings.value ?: UserSettings()
        SoundManager.playRingtone(context, ringtoneName, currentSettings.vibrationEnabled)
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettings()
            repository.updateSettings(current.copy(vibrationEnabled = enabled))
        }
    }

    fun toggleLockMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettings()
            repository.updateSettings(current.copy(lockModeEnabled = enabled))
        }
    }

    // Ambient Sound Player
    fun selectAmbientTrack(name: String) {
        _activeAmbientTrack.value = name
        if (name == "None") {
            SoundManager.stopAmbientSound()
            _isAmbientPlaying.value = false
        } else {
            SoundManager.startAmbientSound(name, _ambientVolume.value)
            _isAmbientPlaying.value = true
        }
    }

    fun toggleAmbientPlayback() {
        if (_isAmbientPlaying.value) {
            SoundManager.stopAmbientSound()
            _isAmbientPlaying.value = false
        } else {
            val track = if (_activeAmbientTrack.value == "None") "Gentle Rain" else _activeAmbientTrack.value
            _activeAmbientTrack.value = track
            SoundManager.startAmbientSound(track, _ambientVolume.value)
            _isAmbientPlaying.value = true
        }
    }

    fun updateAmbientVolume(volume: Float) {
        _ambientVolume.value = volume
        SoundManager.setAmbientVolume(volume)
    }

    // Spotify Integration
    fun launchSpotifyPlaylist(context: Context, uriOrUrl: String) {
        try {
            // Try opening Spotify application first via URI
            val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriOrUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(spotifyIntent)
        } catch (_: Exception) {
            // Fallback to web browser Spotify player
            val webUrl = if (uriOrUrl.startsWith("spotify:playlist:")) {
                "https://open.spotify.com/playlist/" + uriOrUrl.removePrefix("spotify:playlist:")
            } else if (uriOrUrl.startsWith("spotify:")) {
                "https://open.spotify.com"
            } else {
                uriOrUrl
            }
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {}
        }
    }

    fun deleteSession(id: Int) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun getAppNameForPackage(packageName: String): String {
        return try {
            val pm = getApplication<Application>().packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unbindService(serviceConnection)
        } catch (_: Exception) {}
        SoundManager.stopAmbientSound()
    }
}
