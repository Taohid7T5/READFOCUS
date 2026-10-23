package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_sessions")
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookTitle: String,
    val targetMinutes: Int,
    val actualFocusMinutes: Int, // Pure focus time (excluding breaks)
    val breakMinutes: Int,       // Total break time
    val completed: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val focusIntervalMinutes: Int = 30,
    val breakIntervalMinutes: Int = 5,
    val notes: String = ""
)
