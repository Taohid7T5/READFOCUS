package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettings)

    @Query("UPDATE user_settings SET guardianPassword = :password WHERE id = 1")
    suspend fun updateGuardianPassword(password: String)

    @Query("UPDATE user_settings SET selectedRingtone = :ringtone WHERE id = 1")
    suspend fun updateRingtone(ringtone: String)

    @Query("UPDATE user_settings SET spotifyPlaylistUri = :uri WHERE id = 1")
    suspend fun updateSpotifyUri(uri: String)
}
