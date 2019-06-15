package com.eharrison.game.tdcreator

data class Tower(
    private val x: Int,
    private val y: Int,
    private val z: Int = 0,
    private val sizeX: Int = 1,
    private val sizeY: Int = 1,
    private val sizeZ: Int = 1
) {
    val region = Region(x, y, z, x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1)
}

data class Creep(
    private var x: Double,
    private var y: Double,
    private var z: Double = 0.0
) {
    var location = Point(x, y, z)
}

data class Projectile(
    private val x: Double,
    private val y: Double,
    private val z: Double = 0.0
) {
    var location = Point(x, y, z)
}
