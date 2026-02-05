package com.pointlessgames.kroma.dailyChallenge.di

import com.pointlessgames.kroma.Route
import com.pointlessgames.kroma.dailyChallenge.DailyChallengeViewModel
import com.pointlessgames.kroma.dailyChallenge.ui.DailyChallengeScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val dailyChallengeModule = module {
    viewModelOf(::DailyChallengeViewModel)

    navigation<Route.DailyChallenge> {
        DailyChallengeScreen(
            viewModel = koinViewModel(),
        )
    }
}
