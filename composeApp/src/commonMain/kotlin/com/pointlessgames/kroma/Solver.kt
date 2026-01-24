package com.pointlessgames.kroma

import com.pointlessgames.kroma.model.Direction
import com.pointlessgames.kroma.model.LevelData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal object Solver {

    /**
     * Cache to store the shortest move sequence for a given level state.
     */
    private val bestSequenceCache = mutableMapOf<LevelData, List<Direction>>()

    /**
     * Clears the cache.
     */
    fun clearCache() {
        bestSequenceCache.clear()
    }

    /**
     * Finds the best next move for a given level state by finding the shortest solution.
     */
    suspend fun getBestNextMove(levelData: LevelData): Direction? {
        val bestSequence = getBestMoveSequence(levelData)
        // Return the first move of the sequence, or null if no solution exists.
        return bestSequence?.firstOrNull()
    }

    /**
     * Finds the shortest move sequence from a given level state using Breadth-First Search (BFS).
     * This is more efficient for finding the optimal solution than the previous recursive approach.
     *
     * @param levelData The starting state of the level.
     * @return The shortest list of directions to solve the level, or null if no solution is found.
     */
    suspend fun getBestMoveSequence(levelData: LevelData): List<Direction>? =
        withContext(Dispatchers.Default) {
            if (bestSequenceCache.containsKey(levelData)) {
                return@withContext bestSequenceCache[levelData]
            }

            // Use a queue for BFS, storing pairs of (level state, path to reach it)
            val queue = ArrayDeque<Pair<LevelData, List<Direction>>>()
            queue.add(levelData to emptyList())

            // Keep track of visited states to avoid cycles and redundant work.
            val visited = mutableSetOf<LevelData>()
            visited.add(levelData)

            while (currentCoroutineContext().isActive && queue.isNotEmpty()) {
                val (currentLevel, path) = queue.removeFirst()

                // If the current state is finished, we have found the shortest path.
                if (Game.isFinished(currentLevel)) {
                    // Cache and return the solution.
                    bestSequenceCache[levelData] = path
                    return@withContext path
                }

                // Explore all possible moves from the current state.
                Game.getPossibleMoves(currentLevel).forEach { move ->
                    val nextLevelData = Game.performMove(currentLevel, move)

                    // If we haven't visited this state before, add it to the queue.
                    if (visited.add(nextLevelData)) {
                        val newPath = path + move
                        queue.add(nextLevelData to newPath)
                    }
                }
            }

            // If the queue becomes empty and no solution was found, cache that no solution exists.
            bestSequenceCache[levelData] = emptyList() // Representing no solution
            return@withContext null
        }
}
