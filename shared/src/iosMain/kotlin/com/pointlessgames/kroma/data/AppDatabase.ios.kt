package com.pointlessgames.kroma.data

import androidx.room.Room
import androidx.sqlite.driver.NativeSQLiteDriver
import com.pointlessgames.kroma.utils.documentDirectory

internal fun getDatabaseBuilder() = Room.databaseBuilder<AppDatabase>(
    name = documentDirectory() + "/my_room.db",
).setDriver(NativeSQLiteDriver())
