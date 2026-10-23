package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.PermittedAppDao
import com.example.data.dao.ReadingSessionDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.PermittedApp
import com.example.data.model.ReadingSession
import com.example.data.model.UserSettings

@Database(
    entities = [
        ReadingSession::class,
        PermittedApp::class,
        UserSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun permittedAppDao(): PermittedAppDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "read_focus_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
