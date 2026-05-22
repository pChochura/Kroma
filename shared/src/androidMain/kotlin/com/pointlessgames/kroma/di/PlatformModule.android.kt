package com.pointlessgames.kroma.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pointlessgames.kroma.data.AppDatabase
import com.pointlessgames.kroma.data.createDataStore
import com.pointlessgames.kroma.data.createDatabase
import com.pointlessgames.kroma.data.getDatabaseBuilder
import kotlinx.coroutines.runBlocking
import kroma.shared.generated.resources.Res
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<AppDatabase> {
        val sqlStatement = runBlocking {
            Res.readBytes("files/levels.sql").decodeToString()
        }
        createDatabase(getDatabaseBuilder(androidContext()), sqlStatement)
    }
    single<DataStore<Preferences>> { createDataStore(androidContext()) }
}
