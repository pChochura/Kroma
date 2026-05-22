package com.pointlessgames.kroma.start.di

import com.pointlessgames.kroma.Route
import com.pointlessgames.kroma.start.StartViewModel
import com.pointlessgames.kroma.start.ui.StartScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val startModule = module {
    viewModelOf(::StartViewModel)

    navigation<Route.Start> {
        StartScreen(
            viewModel = koinViewModel(),
        )
    }
}
