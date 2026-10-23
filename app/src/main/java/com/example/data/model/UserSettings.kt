package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val guardianPassword: String = "1234",
    val guardianMessage: String = "Please give the password to another person or guardian.",
    val selectedRingtone: String = "Zen Bell",
    val vibrationEnabled: Boolean = true,
    val spotifyPlaylistUri: String = "spotify:playlist:37i9dQZF1DWZeKCadgRdKQ",
    val defaultSessionMinutes: Int = 30,
    val breakIntervalMinutes: Int = 30,
    val breakDurationMinutes: Int = 5,
    val lockModeEnabled: Boolean = true
)
