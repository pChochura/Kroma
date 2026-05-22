package com.pointlessgames.kroma.tutorial.di

import com.pointlessgames.kroma.Route
import com.pointlessgames.kroma.tutorial.TutorialViewModel
import com.pointlessgames.kroma.tutorial.ui.TutorialScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val tutorialModule = module {
    viewModelOf(::TutorialViewModel)

    navigation<Route.Tutorial> {
        TutorialScreen(
            viewModel = koinViewModel(),
        )
    }
}
