package com.pointlessgames.agame.data

import com.pointlessgames.agame.data.dao.LevelDao
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.Position
import kotlinx.serialization.json.Json

internal class LevelRepository(
    private val levelDao: LevelDao = StaticDatabase.levelDao(),
) {
    private val json = Json {
        allowStructuredMapKeys = true
    }

    suspend fun getLevels(): List<LevelData> = levelDao.getAll().map {
        LevelData(
            id = it.id,
            width = it.width,
            height = it.height,
            currentPosition = Position(it.startingPositionX, it.startingPositionY),
            endingPosition = Position(it.endingPositionX, it.endingPositionY),
            tiles = json.decodeFromString(it.tiles),
        )
    }

    suspend fun addLevel(levelData: LevelData) {
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

    suspend fun removeLevel(id: Long) {
        levelDao.remove(id)
    }
}
