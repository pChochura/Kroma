package com.pointlessgames.agame.model

internal data class UndoState(
    val currentPosition: Position,
    val gridTiles: Map<Position, GridTile>,
)
