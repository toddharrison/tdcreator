package com.eharrison.game.tdcreator

data class Tower(
    val region: Region
)

data class Creep(
    var location: PointProperty
)

data class Projectile(
    var location: Point
)
