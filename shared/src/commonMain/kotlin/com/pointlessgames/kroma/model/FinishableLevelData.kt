package com.pointlessgames.kroma.model

import kotlinx.serialization.Serializable

@Serializable
internal data class FinishableLevelData(
    val levelData: LevelData,
    val isFinished: Boolean,
)
