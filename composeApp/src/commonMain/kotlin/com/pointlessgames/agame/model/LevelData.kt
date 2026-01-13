package com.pointlessgames.agame.model

import kotlinx.serialization.Serializable

@Serializable
internal data class LevelData(
    val id: Long = -1,
    val width: Int,
    val height: Int,
    val currentPosition: Position,
    val endingPosition: Position,
    val tiles: Map<Position, GridTile>,
)
