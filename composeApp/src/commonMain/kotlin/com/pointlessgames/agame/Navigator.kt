package com.pointlessgames.agame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal sealed interface Route : NavKey {
    @Serializable
    data object Level : Route

    @Serializable
    data object LevelCreator : Route
}

internal val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Level::class, Route.Level.serializer())
            subclass(Route.LevelCreator::class, Route.LevelCreator.serializer())
        }
    }
}

@Composable
internal fun Navigator(
    backStack: NavBackStack<NavKey>,
    content: EntryProviderScope<NavKey>.() -> Unit,
) {
    CompositionLocalProvider(LocalBackStack provides backStack) {
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider(builder = content),
        )
    }
}

internal val LocalBackStack: ProvidableCompositionLocal<NavBackStack<NavKey>> = compositionLocalOf {
    error("LocalBackStack not initialized")
}
