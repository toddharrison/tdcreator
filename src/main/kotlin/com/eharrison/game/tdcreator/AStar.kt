package com.eharrison.game.tdcreator

import java.util.*
import kotlin.system.measureNanoTime
import java.util.Comparator
import java.util.ArrayList



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
//    println("Press any key to begin...")
//    readLine()

    val start = find(map, START)
    val end = find(map, END)

    val ns = measureNanoTime {
        path.addAll(aStar(start, end, ::getNeighbors, ::distance, blocked(map), ::euclidean))
    }

    printMap()

    if (path.isEmpty()) println("NO PATH AVAILABLE!")

    println()
    println("Search took ${ns / 1000000L} milliseconds")
    println()

//    readLine()
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
    return if (node0.x != node1.x && node0.y != node1.y) 1.0 else 1.0
}

private val blocked: (String) -> (Node) -> Boolean = { map -> { node -> map[index(node.x, node.y)] == SOLID }}

data class Node(
    val x: Int,
    val y: Int
)

fun euclidean(node0: Node, node1: Node): Double {
    val x = node1.x - node0.x
    val y = node1.y - node0.y
    return Math.sqrt((x * x + y * y).toDouble())
}

fun manhattan(node0: Node, node1: Node): Double {
    return (Math.abs(node1.x - node0.x) + Math.abs(node1.y - node0.y)).toDouble()
}



typealias GetNeighbors<N> = (N) -> List<N>
typealias Distance<N> = (N, N) -> Double
typealias Exclude<N> = (N) -> Boolean
typealias Heuristic<N> = (N, N) -> Double

fun <N> aStar(
    start: N,
    goal: N,
    neighbors: GetNeighbors<N>,
    distance: Distance<N>,
    blocked: Exclude<N>,
    heuristic: Heuristic<N>
): List<N> {
    val closedSet = mutableSetOf<N>() // The set of nodes already evaluated.
    val openSet = mutableListOf(start) // The set of tentative nodes to be evaluated, initially containing the start node
    val cameFrom = mutableMapOf<N, N>() // The map of navigated nodes.

    val gScore = mutableMapOf<N, Double>() // Cost from start along best known path.
    gScore[start] = 0.0

    // Estimated total cost from start to goal through y.
    val fScore = mutableMapOf<N, Double>()
    fScore[start] = heuristic(start, goal)

    val comparator = object : Comparator<N> {
        override fun compare(o1: N, o2: N): Int {
            if (fScore[o1] ?: Double.MAX_VALUE < (fScore[o2] ?: Double.MAX_VALUE))
                return -1
            return if (fScore[o2] ?: Double.MAX_VALUE < (fScore[o1] ?: Double.MAX_VALUE)) 1 else 0
        }
    }

    while (!openSet.isEmpty()) {
        val current = openSet.get(0)
        if (current!!.equals(goal))
            return reconstructPath(cameFrom, goal)

        openSet.removeAt(0)
        closedSet.add(current)
        for (neighbor in neighbors(current)) {
            if (closedSet.contains(neighbor) || blocked(neighbor))
                continue // Ignore the neighbor which is already evaluated.

            val tenativeGScore = gScore[current]!! + distance(current, neighbor) // length of this path.
            if (!openSet.contains(neighbor))
                openSet.add(neighbor) // Discover a new node
            else if (tenativeGScore >= gScore[neighbor]!!)
                continue

            // This path is the best until now. Record it!
            cameFrom.put(neighbor, current)
            gScore[neighbor] = tenativeGScore
            val estimatedFScore = gScore[neighbor]!! + heuristic(neighbor, goal)
            fScore[neighbor] = estimatedFScore

            // fScore has changed, re-sort the list
            Collections.sort(openSet, comparator)
        }
    }

    return emptyList()
}

private fun <N> reconstructPath(
    cameFrom: Map<N, N>,
    current: N?
): List<N> {
    var current = current
    val totalPath = ArrayList<N>()

    while (current != null) {
        val previous = current
        current = cameFrom[current]
        if (current != null) {
            totalPath.add(previous)
        }
    }
    Collections.reverse(totalPath)
    return totalPath
}