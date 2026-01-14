package com.pointlessgames.agame.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

internal fun getDatabaseBuilder() = Room.databaseBuilder<AppDatabase>(
    name = File(System.getProperty("java.io.tmpdir"), "my_room.db").absolutePath,
).setDriver(BundledSQLiteDriver())
