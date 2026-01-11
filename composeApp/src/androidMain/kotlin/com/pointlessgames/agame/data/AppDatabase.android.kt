package com.pointlessgames.agame.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.pointlessgames.agame.utils.PlatformContext

actual fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
