package com.pointlessgames.agame.di

import com.pointlessgames.agame.data.AppDatabase
import com.pointlessgames.agame.data.createDatabase
import com.pointlessgames.agame.data.getDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single<AppDatabase> { createDatabase(getDatabaseBuilder()) }
}
