package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.SessionMode
import com.example.ui.components.AppLockedDialog
import com.example.ui.components.GuardianPasswordModal
import com.example.ui.screens.ActiveFocusScreen
import com.example.ui.screens.FocusHomeScreen
import com.example.ui.screens.GuardianSettingsScreen
import com.example.ui.screens.PermittedAppsScreen
import com.example.ui.screens.ReadingStatsScreen
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FocusTimerViewModel

enum class NavigationScreen(val label: String) {
    FOCUS("Focus"),
    PERMITTED_APPS("Apps"),
    SETTINGS("Guardian"),
    STATS("Stats")
}

class MainActivity : ComponentActivity() {

    private val viewModel: FocusTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun MainAppContent(viewModel: FocusTimerViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationScreen.FOCUS) }

    val serviceState by viewModel.serviceState.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedSessionsCount.collectAsStateWithLifecycle()
    val permittedApps by viewModel.permittedApps.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    val isGuardianModalOpen by viewModel.guardianModalVisible.collectAsStateWithLifecycle()
    val guardianInput by viewModel.guardianInput.collectAsStateWithLifecycle()
    val guardianError by viewModel.guardianError.collectAsStateWithLifecycle()

    val activeAmbientTrack by viewModel.activeAmbientTrack.collectAsStateWithLifecycle()
    val isAmbientPlaying by viewModel.isAmbientPlaying.collectAsStateWithLifecycle()
    val ambientVolume by viewModel.ambientVolume.collectAsStateWithLifecycle()

    val hasUsagePermission by viewModel.hasUsagePermission.collectAsStateWithLifecycle()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsStateWithLifecycle()

    val isSessionActive = serviceState.mode == SessionMode.FOCUSING ||
            serviceState.mode == SessionMode.BREAK ||
            serviceState.mode == SessionMode.PAUSED

    // Auto-switch to Focus tab when timer is started
    LaunchedEffect(isSessionActive) {
        if (isSessionActive) {
            currentTab = NavigationScreen.FOCUS
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Focus Tab
                NavigationBarItem(
                    selected = currentTab == NavigationScreen.FOCUS,
                    onClick = { currentTab = NavigationScreen.FOCUS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationScreen.FOCUS) Icons.Filled.Timer else Icons.Outlined.Timer,
                            contentDescription = "Focus Tab",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isSessionActive) "Active" else "Focus",
                            fontWeight = if (currentTab == NavigationScreen.FOCUS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberPrimary,
                        selectedTextColor = AmberPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_focus")
                )

                // Permitted Apps Tab
                NavigationBarItem(
                    selected = currentTab == NavigationScreen.PERMITTED_APPS,
                    onClick = { currentTab = NavigationScreen.PERMITTED_APPS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationScreen.PERMITTED_APPS) Icons.Filled.Apps else Icons.Outlined.Apps,
                            contentDescription = "Apps Tab",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Apps",
                            fontWeight = if (currentTab == NavigationScreen.PERMITTED_APPS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberPrimary,
                        selectedTextColor = AmberPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_apps")
                )

                // Guardian & Sounds Tab
                NavigationBarItem(
                    selected = currentTab == NavigationScreen.SETTINGS,
                    onClick = { currentTab = NavigationScreen.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationScreen.SETTINGS) Icons.Filled.Security else Icons.Outlined.Security,
                            contentDescription = "Guardian Tab",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Guardian",
                            fontWeight = if (currentTab == NavigationScreen.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberPrimary,
                        selectedTextColor = AmberPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_guardian")
                )

                // Reading Stats Tab
                NavigationBarItem(
                    selected = currentTab == NavigationScreen.STATS,
                    onClick = { currentTab = NavigationScreen.STATS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationScreen.STATS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Stats Tab",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Stats",
                            fontWeight = if (currentTab == NavigationScreen.STATS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberPrimary,
                        selectedTextColor = AmberPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_stats")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationScreen.FOCUS -> {
                    if (isSessionActive) {
                        ActiveFocusScreen(
                            state = serviceState,
                            permittedApps = permittedApps,
                            activeAmbientTrack = activeAmbientTrack,
                            isAmbientPlaying = isAmbientPlaying,
                            ambientVolume = ambientVolume,
                            onSelectAmbientTrack = { viewModel.selectAmbientTrack(it) },
                            onToggleAmbientPlay = { viewModel.toggleAmbientPlayback() },
                            onAmbientVolumeChange = { viewModel.updateAmbientVolume(it) },
                            onPause = { viewModel.pauseSession() },
                            onResume = { viewModel.resumeSession() },
                            onSkipBreak = { viewModel.skipBreak() },
                            onRequestEmergencyUnlock = { viewModel.showEmergencyUnlockModal() },
                            onLaunchSpotify = { ctx, uri -> viewModel.launchSpotifyPlaylist(ctx, uri) }
                        )
                    } else {
                        FocusHomeScreen(
                            permittedAppsCount = permittedApps.count { it.isPermitted },
                            activeAmbientTrack = activeAmbientTrack,
                            isAmbientPlaying = isAmbientPlaying,
                            ambientVolume = ambientVolume,
                            defaultFocusDuration = userSettings?.breakIntervalMinutes ?: 30,
                            defaultBreakDuration = userSettings?.breakDurationMinutes ?: 5,
                            onSelectAmbientTrack = { viewModel.selectAmbientTrack(it) },
                            onToggleAmbientPlay = { viewModel.toggleAmbientPlayback() },
                            onAmbientVolumeChange = { viewModel.updateAmbientVolume(it) },
                            onLaunchSpotify = { ctx, uri -> viewModel.launchSpotifyPlaylist(ctx, uri) },
                            onStartFocus = { title, totalMins, cycleMins, breakMins ->
                                viewModel.startFocusSession(title, totalMins, cycleMins, breakMins)
                            },
                            onNavigateToApps = { currentTab = NavigationScreen.PERMITTED_APPS },
                            onNavigateToSettings = { currentTab = NavigationScreen.SETTINGS }
                        )
                    }
                }

                NavigationScreen.PERMITTED_APPS -> {
                    PermittedAppsScreen(
                        apps = permittedApps,
                        hasUsagePermission = hasUsagePermission,
                        hasOverlayPermission = hasOverlayPermission,
                        onTogglePermitted = { pkg, isPermitted ->
                            viewModel.toggleAppPermitted(pkg, isPermitted)
                        },
                        onSetAllPermitted = { isPermitted ->
                            viewModel.setAllAppsPermitted(isPermitted)
                        },
                        onRefreshApps = { viewModel.refreshInstalledApps() },
                        onCheckPermissions = { viewModel.checkPermissions() }
                    )
                }

                NavigationScreen.SETTINGS -> {
                    GuardianSettingsScreen(
                        userSettings = userSettings,
                        onUpdatePassword = { viewModel.updateGuardianPassword(it) },
                        onUpdateRingtone = { viewModel.updateRingtone(it) },
                        onTestRingtone = { viewModel.testRingtone(it) },
                        onToggleVibration = { viewModel.toggleVibration(it) },
                        onToggleLockMode = { viewModel.toggleLockMode(it) },
                        onUpdateSessionDurations = { defaultMins, focusMins, breakMins ->
                            viewModel.updateSessionDurations(defaultMins, focusMins, breakMins)
                        }
                    )
                }

                NavigationScreen.STATS -> {
                    ReadingStatsScreen(
                        sessions = allSessions,
                        totalFocusMinutes = totalFocusMinutes ?: 0,
                        completedCount = completedCount,
                        onDeleteSession = { viewModel.deleteSession(it) }
                    )
                }
            }

            // Guardian Emergency Password Modal
            GuardianPasswordModal(
                isOpen = isGuardianModalOpen,
                guardianInput = guardianInput,
                errorMessage = guardianError,
                onInputChange = { viewModel.onGuardianInputChange(it) },
                onConfirmUnlock = { viewModel.verifyGuardianPasswordAndAbort() },
                onDismiss = { viewModel.dismissEmergencyUnlockModal() }
            )

            // App Blocked Lock Alert Dialog (if unpermitted app attempted during focus session)
            serviceState.blockedAppEncountered?.let { blockedPkg ->
                AppLockedDialog(
                    blockedPackage = blockedPkg,
                    blockedAppName = viewModel.getAppNameForPackage(blockedPkg),
                    onDismiss = { viewModel.clearBlockedAppAlert() },
                    onRequestEmergencyUnlock = {
                        viewModel.clearBlockedAppAlert()
                        viewModel.showEmergencyUnlockModal()
                    }
                )
            }
        }
    }
}
