package com.pointlessgames.agame.di

import com.pointlessgames.agame.data.di.dataModule
import com.pointlessgames.agame.game.di.gameModule
import com.pointlessgames.agame.levelCreator.di.levelCreatorModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

internal val appModule = module {
    includes(platformModule)
    includes(dataModule)

    includes(gameModule)
    includes(levelCreatorModule)
}

fun initKoin(config: KoinAppDeclaration = {}) = startKoin {
    config()
    modules(appModule)
}
