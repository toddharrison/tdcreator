package com.eharrison.game.tdcreator

data class Game(
    val sizeX: Int,
    val sizeY: Int,
    val towers: MutableList<Tower> = mutableListOf()
)

data class Tower(
    val x: Int,
    val y: Int,
    val sizeX: Int = 1,
    val sizeY: Int = 1
)

fun addTower(game: Game, tower: Tower): Boolean {
    if (
        tower.x in 0..game.sizeX - tower.sizeX
        && tower.y in 0..game.sizeY - tower.sizeY
    ) {
        game.towers.add(tower)
        return true
    } else return false
}
