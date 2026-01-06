package com.pointlessgames.agame.model

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val x: Int,
    val y: Int,
)
