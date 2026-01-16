package com.pointlessgames.agame.start.di

import com.pointlessgames.agame.Route
import com.pointlessgames.agame.start.ui.StartScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val startModule = module {
    navigation<Route.Start> {
        StartScreen()
    }
}
