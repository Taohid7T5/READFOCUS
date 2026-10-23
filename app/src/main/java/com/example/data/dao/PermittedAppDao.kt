package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PermittedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface PermittedAppDao {
    @Query("SELECT * FROM permitted_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<PermittedApp>>

    @Query("SELECT * FROM permitted_apps WHERE isPermitted = 1")
    fun getPermittedApps(): Flow<List<PermittedApp>>

    @Query("SELECT isPermitted FROM permitted_apps WHERE packageName = :packageName")
    suspend fun isAppPermitted(packageName: String): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: PermittedApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<PermittedApp>)

    @Query("UPDATE permitted_apps SET isPermitted = :isPermitted WHERE packageName = :packageName")
    suspend fun updateAppPermission(packageName: String, isPermitted: Boolean)

    @Query("UPDATE permitted_apps SET isPermitted = :isPermitted")
    suspend fun updateAllPermissions(isPermitted: Boolean)

    @Query("SELECT COUNT(*) FROM permitted_apps")
    suspend fun getAppCount(): Int
}
