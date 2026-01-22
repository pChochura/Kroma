package com.pointlessgames.kroma.levelCreator.di

import com.pointlessgames.kroma.Route
import com.pointlessgames.kroma.game.ui.TestLevelScreen
import com.pointlessgames.kroma.levelCreator.LevelCreatorViewModel
import com.pointlessgames.kroma.levelCreator.ui.LevelCreatorScreen
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
