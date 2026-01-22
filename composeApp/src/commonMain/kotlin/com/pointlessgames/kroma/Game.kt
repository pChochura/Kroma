package com.pointlessgames.kroma

import com.pointlessgames.kroma.model.Direction
import com.pointlessgames.kroma.model.Direction.BOTTOM
import com.pointlessgames.kroma.model.Direction.LEFT
import com.pointlessgames.kroma.model.Direction.RIGHT
import com.pointlessgames.kroma.model.Direction.TOP
import com.pointlessgames.kroma.model.GridTile
import com.pointlessgames.kroma.model.LevelData
import com.pointlessgames.kroma.model.Position
import com.pointlessgames.kroma.utils.isAllowed

internal object Game {

    fun isFinished(levelData: LevelData): Boolean =
        levelData.currentPosition == levelData.endingPosition

    fun performMove(
        levelData: LevelData,
        moveDirection: Direction,
    ): LevelData = when (moveDirection) {
        LEFT -> performMove(
            levelData = levelData,
            edgeCondition = {
                it.x - 1 >= 0 && levelData.tiles[it.copy(x = it.x - 1)].isAllowed
            },
            transformedPosition = { it.copy(x = it.x - 1) },
            showDirection = moveDirection.opposite,
        )

        RIGHT -> performMove(
            levelData = levelData,
            edgeCondition = {
                it.x + 1 < levelData.width && levelData.tiles[it.copy(x = it.x + 1)].isAllowed
            },
            transformedPosition = { it.copy(x = it.x + 1) },
            showDirection = moveDirection.opposite,
        )

        TOP -> performMove(
            levelData = levelData,
            edgeCondition = {
                it.y - 1 >= 0 && levelData.tiles[it.copy(y = it.y - 1)].isAllowed
            },
            transformedPosition = { it.copy(y = it.y - 1) },
            showDirection = moveDirection.opposite,
        )

        BOTTOM -> performMove(
            levelData = levelData,
            edgeCondition = {
                it.y + 1 < levelData.height && levelData.tiles[it.copy(y = it.y + 1)].isAllowed
            },
            transformedPosition = { it.copy(y = it.y + 1) },
            showDirection = moveDirection.opposite,
        )
    }

    private fun performMove(
        levelData: LevelData,
        edgeCondition: (Position) -> Boolean,
        transformedPosition: (Position) -> Position,
        showDirection: Direction,
    ): LevelData {
        val currentTile = requireNotNull(levelData.tiles[levelData.currentPosition])
        var currentPosition = levelData.currentPosition
        val currentGridTiles = levelData.tiles.toMutableMap()

        val nextPosition = transformedPosition(currentPosition)
        currentGridTiles[nextPosition]?.let {
            return levelData.copy(
                currentPosition = nextPosition,
                tiles = currentGridTiles + (nextPosition to GridTile(
                    value = currentTile.value,
                    showFromDirection = showDirection,
                )),
            )
        }

        var offset = 0
        while (
            edgeCondition(currentPosition) &&
            !currentGridTiles.contains(transformedPosition(currentPosition))
        ) {
            currentPosition = transformedPosition(currentPosition)
            currentGridTiles[currentPosition] = GridTile(
                value = currentTile.value,
                showFromDirection = showDirection,
                animationOffset = offset++,
            )
        }

        if (
            edgeCondition(currentPosition) &&
            currentGridTiles[transformedPosition(currentPosition)]?.value != currentTile.value
        ) {
            currentPosition = transformedPosition(currentPosition)
        }

        return levelData.copy(
            currentPosition = currentPosition,
            tiles = currentGridTiles,
        )
    }

    fun getPossibleMoves(levelData: LevelData): Set<Direction> = buildSet {
        if (
            calculateCanMove(
                levelData = levelData,
                edgeCondition = {
                    it.x - 1 >= 0 && levelData.tiles[it.copy(x = it.x - 1)].isAllowed
                },
                transformedPosition = { it.copy(x = it.x - 1) },
            )
        ) {
            add(LEFT)
        }

        if (
            calculateCanMove(
                levelData = levelData,
                edgeCondition = {
                    it.x + 1 < levelData.width && levelData.tiles[it.copy(x = it.x + 1)].isAllowed
                },
                transformedPosition = { it.copy(x = it.x + 1) },
            )
        ) {
            add(RIGHT)
        }

        if (
            calculateCanMove(
                levelData = levelData,
                edgeCondition = {
                    it.y - 1 >= 0 && levelData.tiles[it.copy(y = it.y - 1)].isAllowed
                },
                transformedPosition = { it.copy(y = it.y - 1) },
            )
        ) {
            add(TOP)
        }

        if (
            calculateCanMove(
                levelData = levelData,
                edgeCondition = {
                    it.y + 1 < levelData.height && levelData.tiles[it.copy(y = it.y + 1)].isAllowed
                },
                transformedPosition = { it.copy(y = it.y + 1) },
            )
        ) {
            add(BOTTOM)
        }
    }

    private fun calculateCanMove(
        levelData: LevelData,
        edgeCondition: (Position) -> Boolean,
        transformedPosition: (Position) -> Position,
    ): Boolean {
        val currentTile = requireNotNull(levelData.tiles[levelData.currentPosition])
        var currentPosition = levelData.currentPosition
        val currentGridTiles = levelData.tiles

        transformedPosition(currentPosition).let { nextPosition ->
            val nextTile = currentGridTiles[nextPosition]
            if (
                !edgeCondition(nextPosition) && nextTile != null &&
                nextTile.isAllowed && nextTile.value != currentTile.value
            ) {
                return true
            }
        }

        while (
            edgeCondition(currentPosition) &&
            !currentGridTiles.contains(transformedPosition(currentPosition))
        ) {
            currentPosition = transformedPosition(currentPosition)
        }

        val nextTile = currentGridTiles[transformedPosition(currentPosition)]
        return edgeCondition(currentPosition) && nextTile != null &&
                nextTile.isAllowed && nextTile.value != currentTile.value
    }
}
