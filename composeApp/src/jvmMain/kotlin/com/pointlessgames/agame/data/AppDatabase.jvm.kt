package com.pointlessgames.agame.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.pointlessgames.agame.utils.PlatformContext
import java.io.File

actual fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
