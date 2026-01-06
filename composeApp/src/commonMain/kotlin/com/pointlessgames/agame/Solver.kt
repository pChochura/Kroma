package com.pointlessgames.agame

import com.pointlessgames.agame.model.Direction
import com.pointlessgames.agame.model.LevelData

internal object Solver {

    /**
     * A cache to store the computed solutions for a given level state.
     * The key is the LevelData and the value is the list of move sequences that solve it.
     */
    private val cache = mutableMapOf<LevelData, List<List<Direction>>>()
    private val bestSequenceCache = mutableMapOf<LevelData, List<Direction>>()

    /**
     * Clears the cache. This should be called whenever a new, unrelated solving process begins,
     * for instance, when starting a new level.
     */
    fun clearCache() {
        cache.clear()
        bestSequenceCache.clear()
    }

    /**
     * Finds the best next move for a given level state.
     *
     * @param levelData The current state of the level.
     */
    fun getBestNextMove(levelData: LevelData): Direction {
        if (bestSequenceCache.contains(levelData)) {
            return bestSequenceCache.getValue(levelData).first()
        }

        val movesSequences = getMoveSequences(levelData)
        val bestMoveSequence = movesSequences.minBy { it.size }
        bestSequenceCache[levelData] = bestMoveSequence

        return bestMoveSequence.first()
    }

    /**
     * Recursively finds all possible move sequences from a given level state, using a cache to store results.
     *
     * @param levelData The current state of the level.
     * @param currentSequence The sequence of moves made to reach the current state.
     * @return A list of all possible solution sequences.
     */
    fun getMoveSequences(
        levelData: LevelData,
        currentSequence: List<Direction> = emptyList(),
    ): List<List<Direction>> {
        // First, check the cache for an existing solution for the current state.
        if (cache.containsKey(levelData)) {
            // If a result is found, adjust the cached sequences by prepending the current move sequence.
            return cache.getValue(levelData).map { currentSequence + it }
        }

        // A list to hold all found solutions starting from this path
        val solutions = mutableListOf<List<Direction>>()

        // Base case: If the level is finished, the current sequence is a valid solution.
        if (Game.isFinished(levelData)) {
            solutions.add(currentSequence)
            // Cache the result for this finished state (an empty sequence from this point).
            cache[levelData] = listOf(emptyList())
            return solutions
        }

        // Get all possible moves from the current state.
        val possibleMoves = Game.getPossibleMoves(levelData)

        // If there are no more moves and the game is not finished, this path is a dead end.
        if (possibleMoves.isEmpty()) {
            // Cache the fact that this state has no solution.
            cache[levelData] = emptyList()
            return emptyList()
        }

        val solutionsForCache = mutableListOf<List<Direction>>()
        // Recursive step: Explore each possible move.
        possibleMoves.forEach { move ->
            // Create a new state by applying the move
            val nextLevelData = Game.performMove(levelData, move)

            // Recursively call the function for the new state. Note that we pass an empty currentSequence
            // because the full sequence will be reconstructed from the cache results.
            val subsequentSolutions = getMoveSequences(
                levelData = nextLevelData,
                currentSequence = emptyList(), // Pass an empty list to simplify caching logic
            )

            // For each solution found from the next state, prepend the current move and add it to our list.
            subsequentSolutions.forEach { sequence ->
                solutionsForCache.add(listOf(move) + sequence)
            }
        }

        // Store the computed solutions for the current state in the cache.
        cache[levelData] = solutionsForCache

        // Reconstruct the full solution paths for the initial caller.
        return solutionsForCache.map { currentSequence + it }
    }
}
