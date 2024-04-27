package com.example.brickbreaker

data class Brick(
    private var isVisible: Boolean,
    var row: Int,
    var column: Int,
    var width: Int,
    var height: Int
) {

    constructor(row: Int, column: Int, width: Int, height: Int) : this(true, row, column, width, height)

    fun setVisible() {
        isVisible = false // Set the brick as invisible
    }

    fun getVisibility(): Boolean {
        return isVisible
    }
}
