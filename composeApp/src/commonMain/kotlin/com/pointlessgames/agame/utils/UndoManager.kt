package com.pointlessgames.agame.utils

internal class UndoManager<T>() {

    private var index: Int = -1
    private val stateStack: MutableList<T> = mutableListOf()

    fun insertState(state: T) {
        if (stateStack.getOrNull(index) == state) return

        if (canRedo()) {
            clearRedoQueue()
        }

        index++
        stateStack.add(state)
    }

    fun undo(): T {
        if (!canUndo()) error("Undo is not possible")

        return stateStack[--index]
    }

    fun redo(): T {
        if (!canRedo()) error("Redo is not possible")

        return stateStack[++index]
    }

    fun canUndo(): Boolean = !canRedo() && index >= 0 || canRedo() && index > 0
    fun canRedo(): Boolean = stateStack.isNotEmpty() && index < stateStack.lastIndex

    private fun clearRedoQueue() {
        // Remove all elements after the index
        stateStack.subList(index + 1, stateStack.size).clear()
    }
}
