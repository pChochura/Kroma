package com.pointlessgames.kroma.tutorial.utils

import com.pointlessgames.kroma.model.GridTile
import com.pointlessgames.kroma.model.LevelData
import com.pointlessgames.kroma.model.Position

internal val levels = listOf(
    LevelData(
        id = 1,
        width = 3,
        height = 1,
        currentPosition = Position(0, 0),
        endingPosition = Position(2, 0),
        tiles = mapOf(
            Position(0, 0) to GridTile(0),
            Position(2, 0) to GridTile(1),
        ),
    ),
    LevelData(
        id = 2,
        width = 2,
        height = 3,
        currentPosition = Position(0, 2),
        endingPosition = Position(1, 0),
        tiles = mapOf(
            Position(1, 0) to GridTile(1),
            Position(0, 2) to GridTile(0),
            Position(1, 2) to GridTile(1),
        ),
    ),
    LevelData(
        id = 3,
        width = 3,
        height = 2,
        currentPosition = Position(0, 1),
        endingPosition = Position(0, 0),
        tiles = mapOf(
            Position(0, 0) to GridTile(0),
            Position(2, 0) to GridTile(0),
            Position(0, 1) to GridTile(0),
            Position(2, 1) to GridTile(1),
        ),
    ),
    LevelData(
        id = 4,
        width = 3,
        height = 3,
        currentPosition = Position(1, 2),
        endingPosition = Position(0, 0),
        tiles = mapOf(
            Position(0, 0) to GridTile(0),
            Position(1, 0) to GridTile(0),
            Position(2, 0) to GridTile(1),
            Position(1, 2) to GridTile(0),
            Position(2, 2) to GridTile(1),
        ),
    ),
)
