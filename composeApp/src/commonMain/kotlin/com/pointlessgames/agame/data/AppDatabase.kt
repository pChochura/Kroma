package com.pointlessgames.agame.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pointlessgames.agame.data.dao.LevelDao
import com.pointlessgames.agame.data.entity.LevelEntity
import com.pointlessgames.agame.utils.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [LevelEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase>

lateinit var StaticDatabase: AppDatabase

fun initializeRoomDatabase(context: PlatformContext) {
    if (!::StaticDatabase.isInitialized) {
        StaticDatabase = getDatabaseBuilder(context)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
