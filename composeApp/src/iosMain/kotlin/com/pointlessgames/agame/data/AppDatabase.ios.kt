package com.pointlessgames.agame.data

import androidx.room.Room
import androidx.sqlite.driver.NativeSQLiteDriver
import com.pointlessgames.agame.utils.documentDirectory

internal fun getDatabaseBuilder() = Room.databaseBuilder<AppDatabase>(
    name = documentDirectory() + "/my_room.db",
).setDriver(NativeSQLiteDriver())
