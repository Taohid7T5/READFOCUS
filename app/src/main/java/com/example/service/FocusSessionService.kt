package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.model.ReadingSession
import com.example.data.repository.FocusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SessionMode {
    IDLE,
    FOCUSING,
    BREAK,
    PAUSED,
    COMPLETED
}

data class FocusServiceState(
    val mode: SessionMode = SessionMode.IDLE,
    val bookTitle: String = "Reading Session",
    val totalTargetMinutes: Int = 30,
    val totalTargetSeconds: Int = 1800,
    val focusCycleTargetSeconds: Int = 1800,   // e.g. 30 mins (1800s), 25m, 45m
    val focusElapsedSeconds: Int = 0,         // Pure focus time (EXCLUDES breaks)
    val currentCycleFocusSeconds: Int = 0,     // 0 to focusCycleTargetSeconds
    val breakElapsedSeconds: Int = 0,          // 0 to breakTargetSeconds
    val breakTargetSeconds: Int = 300,         // e.g. 5 minutes (300s), 10 mins (600s)
    val breaksTakenCount: Int = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val isRingtonePlaying: Boolean = false,
    val blockedAppEncountered: String? = null
)

class FocusSessionService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var blockerJob: Job? = null

    private val _serviceState = MutableStateFlow(FocusServiceState())
    val serviceState: StateFlow<FocusServiceState> = _serviceState.asStateFlow()

    private lateinit var repository: FocusRepository
    private var permittedPackageSet: Set<String> = emptySet()

    inner class LocalBinder : Binder() {
        fun getService(): FocusSessionService = this@FocusSessionService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(applicationContext)
        repository = FocusRepository(
            db.readingSessionDao(),
            db.permittedAppDao(),
            db.userSettingsDao()
        )
        createNotificationChannel()

        serviceScope.launch {
            repository.permittedApps.collect { apps ->
                permittedPackageSet = apps.map { it.packageName }.toSet()
            }
        }
    }

    fun startSession(
        bookTitle: String,
        targetMinutes: Int,
        focusCycleMinutes: Int = 30,
        breakDurationMinutes: Int = 5
    ) {
        val targetSeconds = targetMinutes * 60
        val cycleSeconds = focusCycleMinutes * 60
        val breakSeconds = breakDurationMinutes * 60

        _serviceState.value = FocusServiceState(
            mode = SessionMode.FOCUSING,
            bookTitle = if (bookTitle.isBlank()) "Reading Session" else bookTitle.trim(),
            totalTargetMinutes = targetMinutes,
            totalTargetSeconds = targetSeconds,
            focusCycleTargetSeconds = cycleSeconds,
            focusElapsedSeconds = 0,
            currentCycleFocusSeconds = 0,
            breakElapsedSeconds = 0,
            breakTargetSeconds = breakSeconds,
            breaksTakenCount = 0,
            sessionStartTime = System.currentTimeMillis(),
            blockedAppEncountered = null
        )

        startForeground(NOTIFICATION_ID, buildNotification())
        startTimerLoop()
        startAppBlockerLoop()
    }

    fun pauseSession() {
        if (_serviceState.value.mode == SessionMode.FOCUSING || _serviceState.value.mode == SessionMode.BREAK) {
            _serviceState.value = _serviceState.value.copy(mode = SessionMode.PAUSED)
            updateNotification("Session Paused")
        }
    }

    fun resumeSession() {
        if (_serviceState.value.mode == SessionMode.PAUSED) {
            val targetMode = if (_serviceState.value.breakElapsedSeconds > 0 &&
                _serviceState.value.breakElapsedSeconds < _serviceState.value.breakTargetSeconds) {
                SessionMode.BREAK
            } else {
                SessionMode.FOCUSING
            }
            _serviceState.value = _serviceState.value.copy(mode = targetMode)
            updateNotification("Focus Session Resumed")
        }
    }

    fun emergencyStopSession(authorized: Boolean): Boolean {
        if (authorized) {
            saveCompletedSession(completed = false)
            stopSessionInternal()
            return true
        }
        return false
    }

    fun skipBreakEarly() {
        if (_serviceState.value.mode == SessionMode.BREAK) {
            _serviceState.value = _serviceState.value.copy(
                mode = SessionMode.FOCUSING,
                breakElapsedSeconds = 0,
                currentCycleFocusSeconds = 0
            )
            updateNotification("Reading Focus Resumed")
        }
    }

    fun clearBlockedAppAlert() {
        _serviceState.value = _serviceState.value.copy(blockedAppEncountered = null)
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                val currentState = _serviceState.value

                when (currentState.mode) {
                    SessionMode.FOCUSING -> {
                        val newFocusElapsed = currentState.focusElapsedSeconds + 1
                        val newCycleFocus = currentState.currentCycleFocusSeconds + 1

                        // Check if total target reached
                        if (newFocusElapsed >= currentState.totalTargetSeconds) {
                            _serviceState.value = currentState.copy(
                                focusElapsedSeconds = currentState.totalTargetSeconds,
                                mode = SessionMode.COMPLETED
                            )
                            onSessionCompleted()
                            break
                        }

                        // Check customized focus interval -> trigger customizable break
                        if (newCycleFocus >= currentState.focusCycleTargetSeconds) {
                            _serviceState.value = currentState.copy(
                                focusElapsedSeconds = newFocusElapsed,
                                currentCycleFocusSeconds = 0,
                                breakElapsedSeconds = 0,
                                breaksTakenCount = currentState.breaksTakenCount + 1,
                                mode = SessionMode.BREAK
                            )
                            onBreakStarted()
                        } else {
                            _serviceState.value = currentState.copy(
                                focusElapsedSeconds = newFocusElapsed,
                                currentCycleFocusSeconds = newCycleFocus
                            )
                        }
                        updateNotification(getFormattedTimerText())
                    }

                    SessionMode.BREAK -> {
                        val newBreakElapsed = currentState.breakElapsedSeconds + 1
                        // Those break minutes are NOT counted in the focus session
                        if (newBreakElapsed >= currentState.breakTargetSeconds) {
                            _serviceState.value = currentState.copy(
                                breakElapsedSeconds = 0,
                                currentCycleFocusSeconds = 0,
                                mode = SessionMode.FOCUSING
                            )
                            onBreakFinished()
                        } else {
                            _serviceState.value = currentState.copy(
                                breakElapsedSeconds = newBreakElapsed
                            )
                        }
                        updateNotification("Break Time: ${formatTime(currentState.breakTargetSeconds - newBreakElapsed)}")
                    }

                    SessionMode.PAUSED, SessionMode.IDLE, SessionMode.COMPLETED -> {
                        // Idle or paused
                    }
                }
            }
        }
    }

    private fun startAppBlockerLoop() {
        blockerJob?.cancel()
        blockerJob = serviceScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1500)
                val state = _serviceState.value
                if (state.mode == SessionMode.FOCUSING) {
                    val settings = repository.getSettings()
                    if (settings.lockModeEnabled && AppBlockerManager.hasUsageStatsPermission(applicationContext)) {
                        val foregroundPkg = AppBlockerManager.getForegroundApp(applicationContext)
                        if (foregroundPkg != null &&
                            foregroundPkg != packageName &&
                            !isLauncherOrSystemUI(foregroundPkg) &&
                            !permittedPackageSet.contains(foregroundPkg)
                        ) {
                            // Non-permitted app opened during active reading timer!
                            _serviceState.value = _serviceState.value.copy(
                                blockedAppEncountered = foregroundPkg
                            )
                            // Redirect to MainActivity lock shield
                            val lockIntent = Intent(applicationContext, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("EXTRA_BLOCKED_PACKAGE", foregroundPkg)
                                putExtra("EXTRA_SHOW_LOCK_SHIELD", true)
                            }
                            startActivity(lockIntent)
                        }
                    }
                }
            }
        }
    }

    private fun isLauncherOrSystemUI(pkg: String): Boolean {
        return pkg.contains("launcher") ||
                pkg.contains("systemui") ||
                pkg == "android" ||
                pkg == "com.google.android.apps.nexuslauncher" ||
                pkg == "com.android.launcher3"
    }

    private fun onBreakStarted() {
        serviceScope.launch {
            val settings = repository.getSettings()
            SoundManager.playRingtone(applicationContext, settings.selectedRingtone, settings.vibrationEnabled)
        }
    }

    private fun onBreakFinished() {
        serviceScope.launch {
            val settings = repository.getSettings()
            SoundManager.playRingtone(applicationContext, settings.selectedRingtone, settings.vibrationEnabled)
        }
    }

    private fun onSessionCompleted() {
        serviceScope.launch {
            val settings = repository.getSettings()
            SoundManager.playRingtone(applicationContext, settings.selectedRingtone, settings.vibrationEnabled)
            saveCompletedSession(completed = true)
            updateNotification("Reading Focus Complete! Well done 📖")
        }
    }

    private fun saveCompletedSession(completed: Boolean) {
        serviceScope.launch(Dispatchers.IO) {
            val state = _serviceState.value
            val actualMinutes = (state.focusElapsedSeconds / 60).coerceAtLeast(if (completed) 1 else 0)
            val breakMinutes = state.breaksTakenCount * (state.breakTargetSeconds / 60)

            if (actualMinutes > 0 || completed) {
                repository.saveSession(
                    ReadingSession(
                        bookTitle = state.bookTitle,
                        targetMinutes = state.totalTargetMinutes,
                        actualFocusMinutes = actualMinutes,
                        breakMinutes = breakMinutes,
                        completed = completed,
                        timestamp = state.sessionStartTime,
                        startTime = state.sessionStartTime,
                        endTime = System.currentTimeMillis(),
                        focusIntervalMinutes = state.focusCycleTargetSeconds / 60,
                        breakIntervalMinutes = state.breakTargetSeconds / 60
                    )
                )
            }
        }
    }

    private fun stopSessionInternal() {
        timerJob?.cancel()
        blockerJob?.cancel()
        _serviceState.value = FocusServiceState(mode = SessionMode.IDLE)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun getFormattedTimerText(): String {
        val remaining = (_serviceState.value.totalTargetSeconds - _serviceState.value.focusElapsedSeconds).coerceAtLeast(0)
        return "Focusing: ${formatTime(remaining)}"
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ReadFocus Timer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active reading focus timer and interval breaks"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(subText: String = "Active Focus"): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ReadFocus: ${_serviceState.value.bookTitle}")
            .setContentText(subText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        blockerJob?.cancel()
    }

    companion object {
        const val CHANNEL_ID = "read_focus_service_channel"
        const val NOTIFICATION_ID = 1001
    }
}
