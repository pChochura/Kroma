package com.pointlessgames.agame.game.di

import com.pointlessgames.agame.Route
import com.pointlessgames.agame.game.GameViewModel
import com.pointlessgames.agame.game.ui.GameScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val gameModule = module {
    viewModelOf(::GameViewModel)

    navigation<Route.Game> {
        GameScreen(
            viewModel = koinViewModel(),
        )
    }
}
