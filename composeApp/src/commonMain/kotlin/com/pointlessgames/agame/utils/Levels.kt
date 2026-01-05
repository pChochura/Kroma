package com.pointlessgames.agame.utils

import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.Position

internal val level1 = LevelData(
    width = 7,
    height = 5,
    startingPosition = Position(1, 1),
    endingPosition = Position(2, 3),
    tiles = mapOf(
        Position(0, 0) to GridTile(1),
        Position(6, 0) to GridTile(2),
        Position(1, 1) to GridTile(0),
        Position(5, 1) to GridTile(1),
        Position(0, 2) to GridTile(1),
        Position(1, 2) to GridTile(0),
        Position(5, 2) to GridTile(2),
        Position(2, 3) to GridTile(0),
        Position(5, 3) to GridTile(1),
        Position(6, 3) to GridTile(1),
    ),
)

internal val level2 = LevelData(
    width = 3,
    height = 4,
    startingPosition = Position(1, 2),
    endingPosition = Position(0, 1),
    tiles = mapOf(
        Position(2, 0) to GridTile(1),
        Position(0, 1) to GridTile(0),
        Position(1, 1) to GridTile(0),
        Position(1, 2) to GridTile(0),
        Position(2, 2) to GridTile(1),
    ),
)
