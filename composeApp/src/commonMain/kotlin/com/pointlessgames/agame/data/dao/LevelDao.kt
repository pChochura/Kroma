package com.pointlessgames.agame.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.pointlessgames.agame.data.entity.LevelEntity

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels ORDER BY id ASC")
    suspend fun getAll(): List<LevelEntity>

    @Query("""INSERT INTO levels(width, height, startingPositionX, startingPositionY, endingPositionX, endingPositionY, tiles) VALUES (:width, :height, :startingPositionX, :startingPositionY, :endingPositionX, :endingPositionY, :tiles)""")
    suspend fun insert(
        width: Int,
        height: Int,
        startingPositionX: Int,
        startingPositionY: Int,
        endingPositionX: Int,
        endingPositionY: Int,
        tiles: String,
    )

    @Query("DELETE FROM levels WHERE id = :id")
    suspend fun remove(id: Long)

    @Query("UPDATE levels SET isFinished = 1 WHERE id = :id")
    suspend fun markAsFinished(id: Long)

    @Query("SELECT id FROM levels WHERE isFinished = 0 ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUnfinishedLevelId(): Long
}
