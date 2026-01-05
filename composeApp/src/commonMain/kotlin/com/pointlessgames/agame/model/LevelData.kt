package com.pointlessgames.agame.model

internal data class LevelData(
    val width: Int,
    val height: Int,
    val startingPosition: Position,
    val endingPosition: Position,
    val tiles: Map<Position, GridTile>,
)
