package com.pointlessgames.kroma.data.di

import com.pointlessgames.kroma.data.AppDatabase
import com.pointlessgames.kroma.data.LevelRepository
import com.pointlessgames.kroma.data.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule = module {
    single { get<AppDatabase>().levelDao() }

    singleOf(::LevelRepository)
    singleOf(::SettingsRepository)
}
