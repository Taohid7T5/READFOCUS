package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AmbientSoundPlayerCard
import com.example.ui.components.SpotifyMusicCard
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.SageGreenBreak
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SageGreenLight

data class SessionIntervalPreset(
    val label: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusHomeScreen(
    permittedAppsCount: Int,
    activeAmbientTrack: String,
    isAmbientPlaying: Boolean,
    ambientVolume: Float,
    defaultFocusDuration: Int = 30,
    defaultBreakDuration: Int = 5,
    onSelectAmbientTrack: (String) -> Unit,
    onToggleAmbientPlay: () -> Unit,
    onAmbientVolumeChange: (Float) -> Unit,
    onLaunchSpotify: (Context, String) -> Unit,
    onStartFocus: (bookTitle: String, totalMinutes: Int, focusCycleMinutes: Int, breakDurationMinutes: Int) -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var bookTitle by remember { mutableStateOf("") }
    var totalTargetMinutes by remember { mutableIntStateOf(60) }
    var focusCycleMinutes by remember { mutableIntStateOf(defaultFocusDuration) }
    var breakDurationMinutes by remember { mutableIntStateOf(defaultBreakDuration) }
    var isCustomDurationOpen by remember { mutableStateOf(false) }

    val intervalPresets = listOf(
        SessionIntervalPreset("30m / 5m", 30, 5, "Standard Default"),
        SessionIntervalPreset("25m / 5m", 25, 5, "Pomodoro"),
        SessionIntervalPreset("45m / 10m", 45, 10, "Deep Reading"),
        SessionIntervalPreset("50m / 10m", 50, 10, "Extended Focus"),
        SessionIntervalPreset("60m / 15m", 60, 15, "Long Study")
    )

    val targetPresets = listOf(30, 45, 60, 90, 120, 180)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_home_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card with Generated Aesthetic Reading Art
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.reading_atmosphere),
                            contentDescription = "Reading Atmosphere",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient tint overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "ReadFocus",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Deep reading with Spotify, ambient sounds & app blocker",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }

                    // Guardian Password Reminder Banner
                    Surface(
                        color = AmberPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Guardian Protection",
                                tint = AmberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "“Please give the password to another person or guardian.”",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Book / Reading Target Input
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WHAT ARE YOU READING?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = AmberPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bookTitle,
                        onValueChange = { bookTitle = it },
                        placeholder = { Text("e.g., Atomic Habits, Chapter 4...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Book",
                                tint = AmberPrimary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = CommonImeActionDone()),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("book_title_input")
                    )
                }
            }
        }

        // Focus & Break Duration Customizer Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FOCUS & BREAK INTERVALS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = AmberPrimary
                                )
                            )
                            Text(
                                text = "${focusCycleMinutes}m Focus • ${breakDurationMinutes}m Break",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCustomDurationOpen) AmberPrimary else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable { isCustomDurationOpen = !isCustomDurationOpen }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Custom sliders",
                                    tint = if (isCustomDurationOpen) Color.White else AmberPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCustomDurationOpen) "Presets" else "Custom",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCustomDurationOpen) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isCustomDurationOpen) {
                        // Presets Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(intervalPresets) { preset ->
                                val isSelected = focusCycleMinutes == preset.focusMinutes && breakDurationMinutes == preset.breakMinutes
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AmberPrimary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .clickable {
                                            focusCycleMinutes = preset.focusMinutes
                                            breakDurationMinutes = preset.breakMinutes
                                        }
                                        .testTag("interval_preset_${preset.focusMinutes}_${preset.breakMinutes}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = preset.label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Custom Interactive Sliders
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            // Focus cycle slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Focus Interval Duration",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "$focusCycleMinutes mins",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AmberPrimary)
                                )
                            }
                            Slider(
                                value = focusCycleMinutes.toFloat(),
                                onValueChange = { focusCycleMinutes = it.toInt() },
                                valueRange = 10f..90f,
                                steps = 15,
                                colors = SliderDefaults.colors(
                                    thumbColor = AmberPrimary,
                                    activeTrackColor = AmberPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Break duration slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Break Rest Duration",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "$breakDurationMinutes mins",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SageGreenDark)
                                )
                            }
                            Slider(
                                value = breakDurationMinutes.toFloat(),
                                onValueChange = { breakDurationMinutes = it.toInt() },
                                valueRange = 2f..25f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = SageGreenDark,
                                    activeTrackColor = SageGreenDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total Target Duration Selector
                    Text(
                        text = "TOTAL SESSION GOAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(targetPresets) { target ->
                            val isSelected = totalTargetMinutes == target
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AmberPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AmberPrimary) else null,
                                modifier = Modifier
                                    .clickable { totalTargetMinutes = target }
                                    .testTag("target_goal_${target}m")
                            ) {
                                Text(
                                    text = "${target}m Total",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AmberPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Break info notice
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SageGreenLight.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer Interval",
                                tint = SageGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "A $breakDurationMinutes-minute break triggers every $focusCycleMinutes minutes. Break minutes are NOT counted in the reading focus time.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Spotify Music Card
        item {
            SpotifyMusicCard(
                onLaunchSpotify = onLaunchSpotify
            )
        }

        // Ambient Sound Player Card (Independent volume & soundscapes)
        item {
            AmbientSoundPlayerCard(
                activeTrack = activeAmbientTrack,
                isPlaying = isAmbientPlaying,
                volume = ambientVolume,
                onSelectTrack = onSelectAmbientTrack,
                onTogglePlay = onToggleAmbientPlay,
                onVolumeChange = onAmbientVolumeChange
            )
        }

        // App Lock Status & Permitted Apps Pill
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToApps() }
                    .testTag("permitted_apps_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AmberPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "Permitted Apps",
                                tint = AmberPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Lock Protection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$permittedAppsCount permitted apps • Others locked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = AmberPrimary,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Manage",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Start Reading Focus Button
        item {
            Button(
                onClick = {
                    val title = if (bookTitle.isBlank()) "Reading Focus" else bookTitle
                    onStartFocus(title, totalTargetMinutes, focusCycleMinutes, breakDurationMinutes)
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_focus_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Focus",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Focus ($totalTargetMinutes mins • ${focusCycleMinutes}m/${breakDurationMinutes}m)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

private fun CommonImeActionDone() = ImeAction.Done

