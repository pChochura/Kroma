package com.pointlessgames.kroma

import com.pointlessgames.kroma.model.GridTile
import com.pointlessgames.kroma.model.GridTile.Companion.MAX_VALUE
import com.pointlessgames.kroma.model.GridTile.Companion.MIN_VALUE
import com.pointlessgames.kroma.model.LevelData
import com.pointlessgames.kroma.model.Position
import com.pointlessgames.kroma.utils.next
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.random.Random

internal object Generator {

    private val random = Random(Random.nextInt())

    // --- EVOLUTIONARY ALGORITHM PARAMETERS ---
    private const val POPULATION_SIZE = 500
    private const val MAX_GENERATIONS = 300
    private const val MUTATION_RATE = 0.4 // 40% chance to mutate a new child
    private const val ELITISM_COUNT = 2 // Automatically keep the top 2 fittest levels

    suspend fun generate(width: Int, height: Int): LevelData? = withContext(Dispatchers.Default) {
        runCatching {
            val complexity = (5 + 1.15.pow(width * height / 2)).coerceAtMost(20.0).toInt()

            // 1. INITIALIZATION: Create an initial population of random levels.
            var population = (1..POPULATION_SIZE).map { createRandomInitialLevel(width, height) }

            repeat(MAX_GENERATIONS) { generation ->
                // 2. FITNESS EVALUATION: Calculate a fitness score for each level in the population.
                val scoredPopulation = population.map { level ->
                    val fitness = Solver.getBestMoveSequence(level)?.size ?: 0
                    level to fitness
                }.sortedByDescending { it.second } // Sort from best (fittest) to worst

                val bestInGeneration = scoredPopulation.first()

                // Check for termination condition: If the best level is complex enough, we're done.
                if (bestInGeneration.second >= complexity) {
                    println("Found suitable level in generation $generation.")
                    return@withContext pruneUntouchedTiles(bestInGeneration.first)
                }

                val nextGeneration = mutableListOf<LevelData>()

                // 3. SELECTION & EVOLUTION
                // Elitism: Automatically carry over the best individuals to the next generation.
                nextGeneration.addAll(scoredPopulation.take(ELITISM_COUNT).map { it.first })

                // Create the rest of the new population through crossover and mutation.
                while (currentCoroutineContext().isActive && nextGeneration.size < POPULATION_SIZE) {
                    // Select two parents from the top half of the population (tournament selection).
                    val parent1 = scoredPopulation.take(POPULATION_SIZE / 2).random(random).first
                    val parent2 = scoredPopulation.take(POPULATION_SIZE / 2).random(random).first

                    // 4. CROSSOVER: Create a child by combining the two parents.
                    var child = crossover(parent1, parent2)

                    // 5. MUTATION: Apply random modifications to the child based on the mutation rate.
                    if (random.nextFloat() < MUTATION_RATE) {
                        child = mutate(child)
                    }

                    nextGeneration.add(child)
                }
                population = nextGeneration


                if (!currentCoroutineContext().isActive) {
                    return@withContext pruneUntouchedTiles(bestInGeneration.first)
                }
            }

            // If max generations are reached, return the best level found so far.
            val bestOverall = population
                .map { it to (Solver.getBestMoveSequence(it)?.size ?: 0) }
                .maxByOrNull { it.second }

            return@withContext bestOverall?.takeIf { it.second > 0 }?.first?.let {
                pruneUntouchedTiles(
                    it
                )
            }
        }.getOrNull()
    }

    /**
     * Creates a new child level by combining tiles from two parent levels.
     */
    private fun crossover(parent1: LevelData, parent2: LevelData): LevelData {
        val childTiles = parent1.tiles.toMutableMap()

        // Take roughly half the tiles from the second parent, overwriting the first's.
        parent2.tiles.forEach { (pos, tile) ->
            if (random.nextBoolean()) {
                childTiles[pos] = tile
            }
        }

        // Ensure start and end positions are valid tiles after crossover.
        val validPositions = childTiles.keys.toList()
        if (validPositions.isEmpty()) {
            // Handle the edge case of an empty level after crossover
            return createRandomInitialLevel(parent1.width, parent1.height)
        }
        val startPos =
            if (parent1.currentPosition in validPositions) parent1.currentPosition else validPositions.random(
                random
            )
        val endPos =
            if (parent1.endingPosition in validPositions && parent1.endingPosition != startPos) parent1.endingPosition else (validPositions - startPos).randomOrNull()
                ?: startPos

        return LevelData(
            width = parent1.width,
            height = parent1.height,
            tiles = childTiles,
            currentPosition = startPos,
            endingPosition = endPos
        )
    }

    /**
     * Applies a random modification to a level. This is a wrapper for the existing modifyTiles.
     */
    private fun mutate(level: LevelData): LevelData {
        val modifiedTiles = modifyTiles(
            tiles = level.tiles.mapValues { it.value.value }.toMutableMap(),
            width = level.width,
            height = level.height,
            startingPosition = level.currentPosition,
            endingPosition = level.endingPosition,
        )

        return toLevelData(
            width = level.width,
            height = level.height,
            tiles = modifiedTiles,
            currentPosition = level.currentPosition,
            endingPosition = level.endingPosition,
        )
    }

    private suspend fun pruneUntouchedTiles(levelData: LevelData): LevelData {
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

        return refinedLevel.copy(
            tiles = refinedLevel.tiles.filterValues { it.value != GridTile.Empty.value },
            currentPosition = levelData.currentPosition,
            endingPosition = levelData.endingPosition,
        )
    }

    private fun createRandomInitialLevel(width: Int, height: Int): LevelData {
        val tiles = buildMap {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (random.nextFloat() <= 0.6f) {
                        set(Position(x, y), random.nextInt(MIN_VALUE, MAX_VALUE))
                    }
                }
            }
        }

        val availablePos = tiles.filterValues { it >= 0 }.keys
        val startPos = availablePos.random(random)
        val endPos = (availablePos - startPos).random(random)

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
                tiles[pos] = tiles.getValue(pos).next(0, MAX_VALUE, random.nextInt(1, MAX_VALUE))
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
