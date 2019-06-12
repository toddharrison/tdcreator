package com.eharrison.game.tdcreator

data class Game(
    val sizeX: Int,
    val sizeY: Int,
    val sizeZ: Int = 1,
    val towers: MutableList<Tower> = mutableListOf()
)

data class Tower(
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val sizeX: Int = 1,
    val sizeY: Int = 1,
    val sizeZ: Int = 1
)

fun addTower(game: Game, tower: Tower): Boolean {
    return if ( // Check if tower is within game boundaries
        tower.x in 0..game.sizeX - tower.sizeX
        && tower.y in 0..game.sizeY - tower.sizeY
        && tower.z in 0..game.sizeZ - tower.sizeZ
    ) {
        if ( // Check for overlapping towers
            getTowersAt(game, tower.x, tower.y, tower.z, tower.x + tower.sizeX - 1, tower.y + tower.sizeY - 1, tower.z + tower.sizeZ - 1).isEmpty()
        ) {
            game.towers.add(tower)
        } else false
    } else false
}

fun removeTower(game: Game, tower: Tower): Boolean {
    return game.towers.remove(tower)
}

fun getTowersAt(game: Game, x1: Int, y1: Int, z1: Int = 0, x2: Int = x1, y2: Int = y1, z2: Int = z1): List<Tower> {
    val minX = Math.min(x1, x2)
    val maxX = Math.max(x1, x2)
    val minY = Math.min(y1, y2)
    val maxY = Math.max(y1, y2)
    val minZ = Math.min(z1, z2)
    val maxZ = Math.max(z1, z2)

    return game.towers.filter {
        it.x <= maxX && it.x + it.sizeX - 1 >= minX
        && it.y <= maxY && it.y + it.sizeY - 1 >= minY
        && it.z <= maxZ && it.z + it.sizeZ - 1 >= minZ
    }
}

fun render(game: Game): String {
    val sb = StringBuilder()
    for (z in 0 until game.sizeZ) {
        sb.append("Layer $z:\n")
        for (y in 0 until game.sizeY) {
            for (x in 0 until game.sizeX) {
                if (getTowersAt(game, x, y, z).isEmpty()) {
                    sb.append(" .")
                } else {
                    sb.append(" #")
                }
            }
            sb.append("\n")
        }
        sb.append("\n")
    }
    return sb.toString()
}
