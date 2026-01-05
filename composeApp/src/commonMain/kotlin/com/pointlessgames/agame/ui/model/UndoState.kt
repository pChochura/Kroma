package com.pointlessgames.agame.ui.model

import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.Position

internal data class UndoState(
    val currentPosition: Position,
    val gridTiles: Map<Position, GridTile>,
)
