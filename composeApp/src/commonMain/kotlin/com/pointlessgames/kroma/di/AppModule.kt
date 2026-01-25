package com.pointlessgames.kroma.di

import com.pointlessgames.kroma.data.di.dataModule
import com.pointlessgames.kroma.game.di.gameModule
import com.pointlessgames.kroma.levelCreator.di.levelCreatorModule
import com.pointlessgames.kroma.start.di.startModule
import com.pointlessgames.kroma.tutorial.di.tutorialModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

internal val appModule = module {
    includes(platformModule)
    includes(dataModule)

    includes(startModule)
    includes(gameModule)
    includes(tutorialModule)
    includes(levelCreatorModule)
}

fun initKoin(config: KoinAppDeclaration = {}) = startKoin {
    config()
    modules(appModule)
}
