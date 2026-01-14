package com.pointlessgames.agame.data.di

import com.pointlessgames.agame.data.AppDatabase
import com.pointlessgames.agame.data.LevelRepository
import com.pointlessgames.agame.data.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule = module {
    single { get<AppDatabase>().levelDao() }

//    single {
//        PreferenceDataStoreFactory.createWithPath(
//            produceFile = { producePath().toPath() },
//        )
//    }

    singleOf(::LevelRepository)
    singleOf(::SettingsRepository)
}
