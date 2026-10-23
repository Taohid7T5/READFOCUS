package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PermittedApp
import com.example.service.FocusServiceState
import com.example.service.SessionMode
import com.example.ui.components.AmbientSoundPlayerCard
import com.example.ui.components.BreakSessionCard
import com.example.ui.components.SpotifyMusicCard
import com.example.ui.components.TimerRing
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.SageGreenBreak
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SageGreenLight
import com.example.ui.theme.SpotifyGreen

@Composable
fun ActiveFocusScreen(
    state: FocusServiceState,
    permittedApps: List<PermittedApp>,
    activeAmbientTrack: String,
    isAmbientPlaying: Boolean,
    ambientVolume: Float,
    onSelectAmbientTrack: (String) -> Unit,
    onToggleAmbientPlay: () -> Unit,
    onAmbientVolumeChange: (Float) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkipBreak: () -> Unit,
    onRequestEmergencyUnlock: () -> Unit,
    onLaunchSpotify: (Context, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBreak = state.mode == SessionMode.BREAK
    val isPaused = state.mode == SessionMode.PAUSED

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("active_focus_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBreak) SageGreenLight.copy(alpha = 0.35f) else AmberPrimary.copy(alpha = 0.12f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Book",
                        tint = if (isBreak) SageGreenDark else AmberPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = state.bookTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = if (isBreak) "🌱 5-Minute Break Mode" else "🔒 App Lock Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (isBreak) SageGreenBreak else AmberPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBreak) "Break" else "${state.focusElapsedSeconds / 60}m Logged",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Timer Ring
        TimerRing(
            mode = state.mode,
            focusElapsedSeconds = state.focusElapsedSeconds,
            totalTargetSeconds = state.totalTargetSeconds,
            currentCycleFocusSeconds = state.currentCycleFocusSeconds,
            breakElapsedSeconds = state.breakElapsedSeconds,
            breakTargetSeconds = state.breakTargetSeconds
        )

        // If in Break mode, show Break Card with skip button
        if (isBreak) {
            BreakSessionCard(
                breakElapsedSeconds = state.breakElapsedSeconds,
                breakTargetSeconds = state.breakTargetSeconds,
                onSkipBreak = onSkipBreak
            )
        } else {
            // Focus stats row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PURE FOCUS TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberPrimary
                            )
                        )
                        Text(
                            text = "${state.focusElapsedSeconds / 60} min",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BREAKS TAKEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SageGreenDark
                            )
                        )
                        Text(
                            text = "${state.breaksTakenCount} (${state.breaksTakenCount * 5}m total)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }

        // Permitted Apps Quick Switcher
        val allowedApps = permittedApps.filter { it.isPermitted }
        if (allowedApps.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PERMITTED READING APPS (UNLOCKED)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = AmberPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allowedApps) { app ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .clickable {
                                        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (launchIntent != null) {
                                            context.startActivity(launchIntent)
                                        }
                                    }
                                    .testTag("quick_launch_${app.packageName}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = app.appName,
                                        tint = AmberPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spotify Card
        SpotifyMusicCard(
            onLaunchSpotify = onLaunchSpotify
        )

        // Ambient Sound Player
        AmbientSoundPlayerCard(
            activeTrack = activeAmbientTrack,
            isPlaying = isAmbientPlaying,
            volume = ambientVolume,
            onSelectTrack = onSelectAmbientTrack,
            onTogglePlay = onToggleAmbientPlay,
            onVolumeChange = onAmbientVolumeChange
        )

        // Control Buttons: Pause / Resume & Emergency Stop with Guardian Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { if (isPaused) onResume() else onPause() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) AmberPrimary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isPaused) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("pause_resume_button")
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPaused) "Resume" else "Pause",
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onRequestEmergencyUnlock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralWarning.copy(alpha = 0.15f),
                    contentColor = CoralWarning
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("emergency_stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Guardian Unlock",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End Early (PIN)",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Guardian Quote Tip
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Guardian Info",
                    tint = AmberPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "“Please give the password to another person or guardian.”",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
