package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permitted_apps")
data class PermittedApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isPermitted: Boolean,
    val isSystemApp: Boolean = false,
    val category: String = "General"
)
