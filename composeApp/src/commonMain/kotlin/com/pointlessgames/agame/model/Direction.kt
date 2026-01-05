package com.pointlessgames.agame.model

enum class Direction {
    LEFT, RIGHT, TOP, BOTTOM;

    val opposite: Direction
        get() = when (this) {
            LEFT -> RIGHT
            RIGHT -> LEFT
            TOP -> BOTTOM
            BOTTOM -> TOP
        }
}
