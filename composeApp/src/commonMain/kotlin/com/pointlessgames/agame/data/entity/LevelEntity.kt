package com.pointlessgames.agame.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val width: Int,
    val height: Int,
    val startingPositionX: Int,
    val startingPositionY: Int,
    val endingPositionX: Int,
    val endingPositionY: Int,
    val tiles: String,
)
