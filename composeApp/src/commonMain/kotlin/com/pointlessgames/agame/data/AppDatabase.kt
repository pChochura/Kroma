package com.pointlessgames.agame.data

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.pointlessgames.agame.data.dao.LevelDao
import com.pointlessgames.agame.data.entity.LevelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [LevelEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
        ),
    ],
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

internal fun createDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase = builder
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
