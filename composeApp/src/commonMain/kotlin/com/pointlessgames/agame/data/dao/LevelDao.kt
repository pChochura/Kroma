package com.pointlessgames.agame.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.pointlessgames.agame.data.entity.LevelEntity

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels")
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
}
