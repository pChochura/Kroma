package com.pointlessgames.kroma.di

import com.pointlessgames.kroma.data.AppDatabase
import com.pointlessgames.kroma.data.createDatabase
import com.pointlessgames.kroma.data.getDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single<AppDatabase> { createDatabase(getDatabaseBuilder()) }
}
