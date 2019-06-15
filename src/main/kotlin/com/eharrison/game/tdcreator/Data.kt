package com.eharrison.game.tdcreator

data class Tower(
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val sizeX: Int = 1,
    val sizeY: Int = 1,
    val sizeZ: Int = 1
)

data class Creep(
    var x: Double,
    var y: Double,
    var z: Double = 0.0
//    val health: Int = 10
)

data class Projectile(
    val x: Double,
    val y: Double,
    val z: Double = 0.0,
//    val speedX: Double = 0.0,
//    val speedY: Double = 0.0,
//    val speedZ: Double = 0.0,
    val sizeX: Double = 1.0,
    val sizeY: Double = 1.0,
    val sizeZ: Double = 1.0
//    val lifeTime: Double = 3.0 //time in seconds that the projectile will last
//    val Damage: Int = 10
)
