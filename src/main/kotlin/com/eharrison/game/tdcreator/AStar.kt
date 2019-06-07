package com.eharrison.game.tdcreator

import java.util.PriorityQueue
import java.util.ArrayList

typealias GetNeighbors<N> = (N) -> List<N>
typealias Distance<N> = (N, N) -> Double
typealias Exclude<N> = (N) -> Boolean
typealias Heuristic<N> = (N, N) -> Double

fun <N> aStar(
    start: N,
    goal: N,
    neighbors: GetNeighbors<N>,
    distance: Distance<N> = { _, _ -> 1.0 },
    blocked: Exclude<N> = { _ -> false },
    heuristic: Heuristic<N> = { _, _ -> 0.0 } // Dijkstra's algorithm
): List<N> {
    val closedSet = mutableSetOf<N>() // The set of nodes already evaluated.
    val cameFrom = mutableMapOf<N, N>() // The map of navigated nodes.

    val gScore = mutableMapOf<N, Double>() // Cost from start along best known path.
    gScore[start] = 0.0

    // Estimated total cost from start to goal through y.
    val fScore = mutableMapOf<N, Double>()
    fScore[start] = heuristic(start, goal)

    // The set of tentative nodes to be evaluated, initially containing the start node
    val openSet = PriorityQueue<N> {o1, o2 ->
        when {
            (fScore[o1] ?: Double.MAX_VALUE) < (fScore[o2] ?: Double.MAX_VALUE) -> -1
            (fScore[o2] ?: Double.MAX_VALUE) < (fScore[o1] ?: Double.MAX_VALUE) -> 1
            else -> 0
        }
    }
    openSet.add(start)

    while (openSet.isNotEmpty()) {
        val current = openSet.poll()!!
        if (current == goal) {
            return reconstructPath(cameFrom, goal)
        }

        closedSet.add(current)
        for (neighbor in neighbors(current)) {
            if (closedSet.contains(neighbor) || blocked(neighbor)) {
                continue // Ignore the neighbor which is already evaluated or shouldn't be.
            }

            val tentativeGScore = gScore[current]!! + distance(current, neighbor) // Length of this path.
            if (!openSet.contains(neighbor)) {
                openSet.add(neighbor) // Discovered a new node
            }
            else if (tentativeGScore >= gScore[neighbor]!!) {
                continue // Found worse path, ignore.
            }

            // This path is the best until now. Record it!
            cameFrom[neighbor] = current
            gScore[neighbor] = tentativeGScore
            val estimatedFScore = tentativeGScore + heuristic(neighbor, goal)
            fScore[neighbor] = estimatedFScore
        }
    }

    return emptyList()
}

private fun <N> reconstructPath(
    cameFrom: Map<N, N>,
    current: N?
): List<N> {
    var cur = current
    val totalPath = ArrayList<N>()

    while (cur != null) {
        val previous = cur
        cur = cameFrom[cur]
        if (cur != null) {
            totalPath.add(previous)
        }
    }
    totalPath.reverse()
    return totalPath
}
