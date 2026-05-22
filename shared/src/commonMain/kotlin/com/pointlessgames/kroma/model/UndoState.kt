package com.pointlessgames.kroma.model

internal data class UndoState(
    val currentPosition: Position,
    val gridTiles: Map<Position, GridTile>,
)
