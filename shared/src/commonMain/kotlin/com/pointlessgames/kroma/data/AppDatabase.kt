package com.pointlessgames.kroma.data

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.pointlessgames.kroma.data.dao.LevelDao
import com.pointlessgames.kroma.data.entity.LevelEntity
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

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

internal fun createDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    sqlStatement: String,
): AppDatabase = builder
    .addCallback(
        object : RoomDatabase.Callback() {
            override fun onCreate(connection: SQLiteConnection) {
                super.onCreate(connection)
                connection.execSQL(sqlStatement)
            }
        },
    )
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
