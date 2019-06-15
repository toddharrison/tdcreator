package com.eharrison.game.tdcreator

data class Game(
    val sizeX: Int,
    val sizeY: Int,
    val sizeZ: Int = 1,
    val towers: MutableList<Tower> = mutableListOf(),
    val creeps: MutableList<Creep> = mutableListOf(),
    val projectiles: MutableList<Projectile> = mutableListOf()
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


fun addCreep(game: Game, creep: Creep): Boolean {
    val intX = creep.x.toInt()
    val intY = creep.y.toInt()
    val intZ = creep.z.toInt()
    return if (getTowersAt(game, intX,intY,intZ).isEmpty() && intX in 0 until game.sizeX
        && intY in 0 until game.sizeY && intZ in 0 until game.sizeZ) {
        game.creeps.add(creep)
    } else false
}

fun removeCreep(game: Game, creep: Creep): Boolean {
    return game.creeps.remove(creep)
}

fun getCreepsAt(game: Game, x1: Int, y1: Int, z1: Int = 0, x2: Int = x1, y2: Int = y1, z2: Int = z1): List<Creep> {
    val minX = Math.min(x1, x2)
    val maxX = Math.max(x1, x2) + 1
    val minY = Math.min(y1, y2)
    val maxY = Math.max(y1, y2) + 1
    val minZ = Math.min(z1, z2)
    val maxZ = Math.max(z1, z2) + 1

    return game.creeps.filter {
        it.x < maxX && it.x >= minX
                && it.y < maxY && it.y >= minY
                && it.z < maxZ && it.z >= minZ
    }
}


fun addProjectile(game: Game, projectile: Projectile): Boolean {
    val x = projectile.x.toInt()
    val y = projectile.x.toInt()
    val z = projectile.x.toInt()
    val maxX = projectile.sizeX.toInt()
    val maxY = projectile.sizeY.toInt()
    val maxZ = projectile.sizeZ.toInt()
    return if ( // Check if projectile is within game boundaries
        x in 0..game.sizeX - maxX
        && y in 0..game.sizeY - maxY
        && z in 0..game.sizeZ - maxZ
    ) {
        game.projectiles.add(projectile)
    } else false
}


fun render(game: Game): String {
    val sb = StringBuilder()
    for (z in 0 until game.sizeZ) {
        sb.append("Layer $z:\n")
        for (y in 0 until game.sizeY) {
            for (x in 0 until game.sizeX) {
                when {
                    getTowersAt(game, x, y, z).isNotEmpty() -> sb.append(" #")
                    getCreepsAt(game, x, y, z).isNotEmpty() -> sb.append(" *")
                    else -> sb.append(" .")
                }
            }
            sb.append("\n")
        }
        sb.append("\n")
    }
    return sb.toString()
}
