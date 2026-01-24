package com.pointlessgames.kroma.data

import com.pointlessgames.kroma.data.dao.LevelDao
import com.pointlessgames.kroma.model.LevelData
import com.pointlessgames.kroma.model.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class LevelRepository(
    private val levelDao: LevelDao,
) {
    private val json = Json {
        allowStructuredMapKeys = true
    }

    suspend fun getLevels(): List<LevelData> = withContext(Dispatchers.IO) {
        levelDao.getAll().map {
            LevelData(
                id = it.id,
                width = it.width,
                height = it.height,
                currentPosition = Position(it.startingPositionX, it.startingPositionY),
                endingPosition = Position(it.endingPositionX, it.endingPositionY),
                tiles = json.decodeFromString(it.tiles),
            )
        }
    }

    suspend fun addLevel(levelData: LevelData) = withContext(Dispatchers.IO) {
        levelDao.insert(
            width = levelData.width,
            height = levelData.height,
            startingPositionX = levelData.currentPosition.x,
            startingPositionY = levelData.currentPosition.y,
            endingPositionX = levelData.endingPosition.x,
            endingPositionY = levelData.endingPosition.y,
            tiles = json.encodeToString(levelData.tiles),
        )
    }

    suspend fun removeLevel(id: Long) = withContext(Dispatchers.IO) {
        levelDao.remove(id)
    }

    suspend fun markLevelAsFinished(id: Long) = withContext(Dispatchers.IO) {
        levelDao.markAsFinished(id)
    }

    suspend fun getFirstUnfinishedLevelId(): Long = withContext(Dispatchers.IO) {
        levelDao.getFirstUnfinishedLevelId()
    }
}
