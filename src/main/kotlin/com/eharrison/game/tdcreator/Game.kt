package com.eharrison.game.tdcreator

data class Game(
    private val sizeX: Int,
    private val sizeY: Int,
    private val sizeZ: Int = 1,
    val towers: MutableList<Tower> = mutableListOf(),
    val creeps: MutableList<Creep> = mutableListOf(),
    val projectiles: MutableList<Projectile> = mutableListOf()
) {
    val region = Region(0, 0, 0, sizeX - 1, sizeY - 1, sizeZ - 1)
}



fun addTower(game: Game, tower: Tower): Boolean {
    return if (tower.region in game.region && getTowersAt(game, tower.region).isEmpty()) {
        game.towers.add(tower)
    } else false
}
fun removeTower(game: Game, tower: Tower): Boolean = game.towers.remove(tower)
fun getTowersAt(game: Game, region: Region): List<Tower> = game.towers.filter { it.region intersects region }

fun addCreep(game: Game, creep: Creep): Boolean {
    return if (creep.location in game.region && getTowersAt(game, creep.location.toRegion()).isEmpty()) {
        game.creeps.add(creep)
    } else false
}
fun removeCreep(game: Game, creep: Creep): Boolean = game.creeps.remove(creep)
fun getCreepsAt(game: Game, region: Region): List<Creep> = game.creeps.filter { it.location in region }

fun addProjectile(game: Game, projectile: Projectile): Boolean {
    return if (projectile.location in game.region) {
        game.projectiles.add(projectile)
    } else false
}

fun render(game: Game): String {
    val sb = StringBuilder()
    for (z in 0..game.region.maxZ) {
        sb.append("Layer $z:\n")
        for (y in 0..game.region.maxY) {
            for (x in 0..game.region.maxX) {
                val region = Region(x, y, z)
                when {
                    getTowersAt(game, region).isNotEmpty() -> sb.append(" #")
                    getCreepsAt(game, region).isNotEmpty() -> sb.append(" *")
                    else -> sb.append(" .")
                }
            }
            sb.append("\n")
        }
        sb.append("\n")
    }
    return sb.toString()
}
