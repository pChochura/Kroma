package com.pointlessgames.agame.levelCreator.di

import com.pointlessgames.agame.Route
import com.pointlessgames.agame.game.ui.TestLevelScreen
import com.pointlessgames.agame.levelCreator.LevelCreatorViewModel
import com.pointlessgames.agame.levelCreator.ui.LevelCreatorScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val levelCreatorModule = module {
    viewModelOf(::LevelCreatorViewModel)

    navigation<Route.LevelCreator> {
        LevelCreatorScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.TestLevel> {
        TestLevelScreen(
            levelData = it.levelData,
            viewModel = koinViewModel(),
        )
    }
}
