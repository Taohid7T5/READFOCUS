package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.repository.FocusRepository
import com.example.service.AppBlockerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadFocusApp : Application() {

    lateinit var repository: FocusRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = FocusRepository(
            db.readingSessionDao(),
            db.permittedAppDao(),
            db.userSettingsDao()
        )

        // Seed initial apps and settings if first run
        CoroutineScope(Dispatchers.IO).launch {
            repository.getSettings() // Ensure default settings row exists

            if (repository.getAppCount() == 0) {
                val installedApps = AppBlockerManager.loadInstalledApps(this@ReadFocusApp)
                if (installedApps.isNotEmpty()) {
                    repository.insertApps(installedApps)
                }
            }
        }
    }
}
