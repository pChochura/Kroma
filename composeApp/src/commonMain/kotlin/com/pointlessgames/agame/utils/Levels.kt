package com.pointlessgames.agame.utils

import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.Position

internal val levels = mutableListOf(
    LevelData(
        width = 3,
        height = 3,
        currentPosition = Position(0, 0),
        endingPosition = Position(2, 1),
        tiles = mapOf(
            Position(0, 0) to GridTile(0),
            Position(2, 1) to GridTile(0),
            Position(0, 2) to GridTile(1),
        ),
    ),
    LevelData(
        width = 5,
        height = 3,
        currentPosition = Position(0, 2),
        endingPosition = Position(4, 1),
        tiles = mapOf(
            Position(0, 0) to GridTile(1),
            Position(3, 0) to GridTile(1),
            Position(4, 0) to GridTile(0),
            Position(1, 1) to GridTile(1),
            Position(4, 1) to GridTile(0),
            Position(0, 2) to GridTile(0),
            Position(4, 2) to GridTile(0),
        ),
    ),
    LevelData(
        width = 5,
        height = 3,
        currentPosition = Position(0, 0),
        endingPosition = Position(4, 0),
        tiles = mapOf(
            Position(0, 0) to GridTile(0),
            Position(1, 0) to GridTile(0),
            Position(3, 0) to GridTile(2),
            Position(4, 0) to GridTile(2),
            Position(0, 2) to GridTile(1),
            Position(1, 2) to GridTile(0),
            Position(3, 2) to GridTile(1),
            Position(4, 2) to GridTile(0),
        ),
    ),
    LevelData(
        width = 4,
        height = 4,
        currentPosition = Position(1, 1),
        endingPosition = Position(3, 0),
        tiles = mapOf(
            Position(0, 0) to GridTile(0),
            Position(3, 0) to GridTile(1),
            Position(1, 1) to GridTile(0),
            Position(2, 1) to GridTile(1),
            Position(0, 2) to GridTile(2),
            Position(1, 3) to GridTile(2),
            Position(2, 3) to GridTile(0),
        ),
    ),
    LevelData(
        width = 5,
        height = 6,
        currentPosition = Position(0, 0),
        endingPosition = Position(4, 5),
        tiles = mapOf(
            Position(0, 0) to GridTile(0),
            Position(2, 0) to GridTile(0),
            Position(4, 0) to GridTile(0),
            Position(1, 1) to GridTile(0),
            Position(2, 1) to GridTile(1),
            Position(4, 1) to GridTile(1),
            Position(3, 3) to GridTile(1),
            Position(4, 3) to GridTile(1),
            Position(0, 4) to GridTile(1),
            Position(1, 4) to GridTile(0),
            Position(4, 5) to GridTile(0),
        ),
    ),
    LevelData(
        width = 3,
        height = 3,
        currentPosition = Position(0, 0),
        endingPosition = Position(2, 0),
        tiles = mapOf(
            Position(0, 0) to GridTile(1),
            Position(1, 0) to GridTile.Wall,
            Position(2, 0) to GridTile(2),
            Position(2, 1) to GridTile(0),
            Position(0, 2) to GridTile(0),
            Position(2, 2) to GridTile(2),
        ),
    ),
)
