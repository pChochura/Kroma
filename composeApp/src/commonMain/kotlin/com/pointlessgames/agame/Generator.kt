package com.pointlessgames.agame

import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.GridTile.Companion.MAX_VALUE
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.Position
import kotlin.random.Random

internal object Generator {

    private val random = Random(Random.nextInt())
    private const val MIN_MOVES_COMPLEXITY = 8

    fun generate(width: Int, height: Int): LevelData? {
        // 1. Initial setup: Create a mostly random board.
        var bestLevel = createRandomInitialLevel(width, height)
        var bestScore = Solver.getBestMoveSequence(bestLevel)?.size ?: -1

        // 2. Iteratively try to improve the level.
        // We'll try a fixed number of times to avoid an infinite loop.
        repeat(1000) {
            if (bestScore >= MIN_MOVES_COMPLEXITY) {
                // We found a good enough level.
                return pruneUntouchedTiles(bestLevel)
            }

            // Create a temporary copy to modify.
            val currentTiles = bestLevel.tiles.toMutableMap()
            val currentStartingPosition = bestLevel.currentPosition
            val currentEndingPosition = bestLevel.endingPosition

            // 3. Make a random modification to the level.
            val modifiedTiles = modifyTiles(
                tiles = currentTiles.mapValues { it.value.value }.toMutableMap(),
                width = width,
                height = height,
                startingPosition = currentStartingPosition,
                endingPosition = currentEndingPosition,
            )

            val nextLevel = toLevelData(
                width = width,
                height = height,
                tiles = modifiedTiles,
                currentPosition = currentStartingPosition,
                endingPosition = currentEndingPosition,
            )

            // 4. Evaluate the new level.
            val newScore = Solver.getBestMoveSequence(nextLevel)?.size ?: -1

            // 5. Decide if the modification was an improvement.
            // We are looking for a higher score (more moves).
            if (newScore > bestScore) {
                bestScore = newScore
                bestLevel = nextLevel
            }
        }

        // Return the best level found, even if it doesn't meet the criteria,
        // or null if no solution was ever found.
        return if (bestScore > 0) pruneUntouchedTiles(bestLevel) else null
    }

    private fun pruneUntouchedTiles(levelData: LevelData): LevelData {
        val solutionSequence = Solver.getBestMoveSequence(levelData) ?: return levelData
        var currentLevelData = levelData

        // 1. Find all positions that are essential to the solution.
        val touchedPositions = mutableSetOf(
            currentLevelData.currentPosition,
            currentLevelData.endingPosition,
        )

        solutionSequence.forEach { move ->
            currentLevelData = Game.performMove(currentLevelData, move)
            touchedPositions.add(currentLevelData.currentPosition)
        }

        // 2. Create the set of pruned tiles.
        val removedTiles = levelData.tiles
            .filterKeys { it !in touchedPositions }
            .toList()
            .toMutableList()

        var bestScore = solutionSequence.size
        var refinedLevel = currentLevelData.copy(
            tiles = levelData.tiles.filterKeys { it in touchedPositions },
        )

        var index = 0
        while (index < removedTiles.size && removedTiles.isNotEmpty()) {
            val currentTile = removedTiles[index]
            val changedLevelData = currentLevelData.copy(
                tiles = currentLevelData.tiles + (currentTile.first to currentTile.second),
            )

            val newScore = Solver.getBestMoveSequence(changedLevelData)?.size ?: -1
            if (newScore > bestScore) {
                bestScore = newScore
                refinedLevel = changedLevelData

                removedTiles.remove(currentTile)
                index = 0
            } else {
                index++
            }
        }

        refinedLevel = refinedLevel.copy(
            tiles = refinedLevel.tiles.filterValues { it.value != GridTile.Empty.value },
        )

        // --- NEW LOGIC STARTS HERE ---
        val minX = refinedLevel.tiles.keys.minOf { it.x }
        val minY = refinedLevel.tiles.keys.minOf { it.y }
        val maxX = refinedLevel.tiles.keys.maxOf { it.x }
        val maxY = refinedLevel.tiles.keys.maxOf { it.y }

        // 3. Calculate new dimensions.
        val newWidth = maxX - minX + 1
        val newHeight = maxY - minY + 1

        // 4. Re-map all positions and tiles to be relative to the new origin (minX, minY).
        val remappedTiles = refinedLevel.tiles.mapKeys { (pos, _) ->
            Position(pos.x - minX, pos.y - minY)
        }
        val newStartPosition = Position(
            levelData.currentPosition.x - minX,
            levelData.currentPosition.y - minY,
        )
        val newEndPosition = Position(
            levelData.endingPosition.x - minX,
            levelData.endingPosition.y - minY,
        )

        // 5. Return the final, compact level.
        return LevelData(
            width = newWidth,
            height = newHeight,
            tiles = remappedTiles,
            currentPosition = newStartPosition,
            endingPosition = newEndPosition,
        )
    }

    private fun createRandomInitialLevel(width: Int, height: Int): LevelData {
        val tiles = buildMap {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (random.nextFloat() <= 0.6f) {
                        set(Position(x, y), random.nextInt(0, MAX_VALUE))
                    }
                }
            }
        }

        val startPos = tiles.keys.random(random)
        val endPos = (tiles.keys - startPos).random(random)

        return toLevelData(width, height, tiles, startPos, endPos)
    }

    private fun modifyTiles(
        tiles: MutableMap<Position, Int>,
        width: Int,
        height: Int,
        startingPosition: Position,
        endingPosition: Position,
    ): Map<Position, Int> {
        val allPositions =
            (0 until width).flatMap { x -> (0 until height).map { y -> Position(x, y) } }

        when (random.nextInt(4)) {
            // 0: Add a new tile to an empty spot
            0 -> {
                val emptySpots = allPositions - tiles.keys
                if (emptySpots.isNotEmpty()) {
                    tiles[emptySpots.random(random)] = random.nextInt(0, MAX_VALUE)
                }
            }
            // 1: Remove an existing tile
            1 -> {
                val filledSpots = tiles.keys - startingPosition - endingPosition
                if (filledSpots.isNotEmpty()) {
                    tiles.remove(filledSpots.random(random))
                }
            }
            // 2: Change the value of an existing tile
            2 -> if (tiles.isNotEmpty()) {
                val pos = tiles.keys.random(random)
                tiles[pos] = (tiles.getValue(pos) + random.nextInt(1, MAX_VALUE)) % MAX_VALUE
            }
            // Insert a wall at a random empty space
            3 -> {
                val emptySpots = allPositions - tiles.keys
                if (emptySpots.isNotEmpty()) {
                    tiles[emptySpots.random(random)] = GridTile.Wall.value
                }
            }
        }
        return tiles
    }

    private fun toLevelData(
        width: Int,
        height: Int,
        tiles: Map<Position, Int>,
        currentPosition: Position,
        endingPosition: Position = Position(-1, -1),
    ) = LevelData(
        width = width,
        height = height,
        tiles = tiles.mapValues { GridTile(it.value) },
        currentPosition = currentPosition,
        endingPosition = endingPosition,
    )
}
