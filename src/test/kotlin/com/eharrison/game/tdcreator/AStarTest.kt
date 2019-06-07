package com.eharrison.game.tdcreator

import kotlin.system.measureNanoTime

const val WIDTH = 20
const val HEIGHT = 20
const val EMPTY = '.'
const val SOLID = '*'
const val START = 'O'
const val END = 'X'

fun main(args: Array<String>) {
    //@formatter:off
    val map =
        "********************" +
        "*..................*" +
        "*.O************....*" +
        "*.**...............*" +
        "*.*................*" +
        "*.*........*********" +
        "*.*................*" +
        "*.**************...*" +
        "***................*" +
        "*...****************" +
        "*.....*....*.......*" +
        "*......*....*......*" +
        "*.......*....*.....*" +
        "*........*....*....*" +
        "*.........*....*...*" +
        "*..........*....*..*" +
        "*...........*......*" +
        "*............*...X.*" +
        "*..................*" +
        "********************"
    //@formatter:on

    val path = arrayListOf<Node>()

    fun printMap() {
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                if (path.contains(Node(x, y))) print('#')
                else print(map[y * WIDTH + x])
            }

            println()
        }
    }

    printMap()

    println()

    val start = find(map, START)
    val end = find(map, END)

    val ns = measureNanoTime {
        path.addAll(aStar(start, end, ::getNeighbors, ::distance, blocked(map), euclidean))
    }

    printMap()

    if (path.isEmpty()) println("NO PATH AVAILABLE!")

    println()
    println("Search took ${ns / 1000000L} milliseconds")
    println()
}

private fun index(x: Int, y: Int) = y * WIDTH + x

private fun find(map: String, c: Char): Node {
    for (y in 0 until HEIGHT) {
        for (x in 0 until WIDTH) {
            if (map[index(x, y)] == c) return Node(x, y)
        }
    }
    return Node(-1, -1)
}



fun getNeighbors(current: Node): List<Node> {
    val neighbors = mutableListOf<Node>()
    for (y in -1..1) {
        for (x in -1..1) {
            if (x == 0 && y == 0) continue
            val neighbor = Node(current.x + x, current.y + y)
            neighbors.add(neighbor)
        }
    }
    return neighbors
}

private fun distance(node0: Node, node1: Node): Double {
//    return if (node0.x != node1.x && node0.y != node1.y) Math.sqrt(2.0) else 1.0
    return if (node0.x != node1.x && node0.y != node1.y) Math.sqrt(2.0) else 1.0
}

private val blocked: (String) -> (Node) -> Boolean = { map -> { node -> map[index(node.x, node.y)] == SOLID }}

data class Node(
    val x: Int,
    val y: Int
)



val euclidean: (Node, Node) -> Double = { node0, node1 ->
    val x = node1.x - node0.x
    val y = node1.y - node0.y
    Math.sqrt((x * x + y * y).toDouble())
}

val manhattan: (Node, Node) -> Double = { node0, node1 -> (Math.abs(node1.x - node0.x) + Math.abs(node1.y - node0.y)).toDouble() }
