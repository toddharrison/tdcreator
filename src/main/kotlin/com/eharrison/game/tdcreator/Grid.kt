package com.eharrison.game.tdcreator

import kotlin.math.pow

data class Region(
    private val x1: Int,
    private val y1: Int,
    private val z1: Int = 0,
    private val x2: Int = x1,
    private val y2: Int = y1,
    private val z2: Int = z1
) {
    val minX = Math.min(x1, x2)
    val maxX = Math.max(x1, x2)
    val minY = Math.min(y1, y2)
    val maxY = Math.max(y1, y2)
    val minZ = Math.min(z1, z2)
    val maxZ = Math.max(z1, z2)

    operator fun contains(region: Region): Boolean =
        region.minX >= minX
                && region.maxX <= maxX
                && region.minY >= minY
                && region.maxY <= maxY
                && region.minZ >= minZ
                && region.maxZ <= maxZ
    operator fun contains(point: Point): Boolean =
        point.x >= minX
                && point.x < maxX + 1
                && point.y >= minY
                && point.y < maxY + 1
                && point.z >= minZ
                && point.z < maxZ + 1
}

data class Vector(
    val x: Double,
    val y: Double,
    val z: Double = 0.0
) {
    operator fun plus(v: Vector) = Vector(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector) = Vector(x - v.x, y - v.y, z - v.z)
    operator fun times(scalar: Double) = Vector(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Double) = Vector(x / scalar, y / scalar, z / scalar)

    fun magnitude() = magnitudeSquared().pow(0.5)
    fun magnitudeSquared() = (x.pow(2) + y.pow(2) + z.pow(2))
    fun toUnitVector(): Vector {
        return this / magnitude()
    }
}

data class Point(
    val x: Double,
    val y: Double,
    val z: Double = 0.0
) {
    operator fun plus(v: Vector) = Point(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector) = Point(x - v.x, y - v.y, z - v.z)

    fun distance(point: Point) = distanceSquared(point).pow(0.5)
    fun distanceSquared(point: Point) = (point.x - x).pow(2) + (point.y - y).pow(2) + (point.z - z).pow(2)
    fun toRegion() = Region(x.toInt(), y.toInt(), z.toInt())
}

infix fun Region.intersects(region: Region): Boolean =
    (region.minX..region.maxX intersect minX..maxX).isNotEmpty()
            && (region.minY..region.maxY intersect minY..maxY).isNotEmpty()
            && (region.minZ..region.maxZ intersect minZ..maxZ).isNotEmpty()
