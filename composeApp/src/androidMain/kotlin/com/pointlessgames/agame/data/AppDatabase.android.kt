package com.pointlessgames.agame.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver

internal fun getDatabaseBuilder(context: Context) = Room.databaseBuilder<AppDatabase>(
    context = context,
    name = context.getDatabasePath("my_room.db").absolutePath,
).setDriver(AndroidSQLiteDriver())
