package com.eharrison.game.tdcreator

import java.util.PriorityQueue
import java.util.ArrayList

typealias Neighbors<Node> = (Node) -> List<Node>
typealias Cost<Node> = (Node, Node) -> Double
typealias Exclude<Node> = (Node) -> Boolean
typealias Heuristic<Node> = (Node, Node) -> Double

fun <Node> aStar(
    start: Node,
    goal: Node,
    neighbors: Neighbors<Node>,
    cost: Cost<Node> = { _, _ -> 1.0 },
    exclude: Exclude<Node> = { _ -> false },
    heuristic: Heuristic<Node> = { _, _ -> 0.0 } // Dijkstra's algorithm, only for positive costs
): List<Node> {
    val closedSet = mutableSetOf<Node>() // The set of nodes already evaluated.
    val cameFrom = mutableMapOf<Node, Node>() // The map of navigated nodes.

    val gScore = mutableMapOf<Node, Double>() // Cost from start along best known path.
    gScore[start] = 0.0

    // Estimated total cost from start to goal through y.
    val fScore = mutableMapOf<Node, Double>()
    fScore[start] = heuristic(start, goal)

    // The set of tentative nodes to be evaluated, initially containing the start node
    val openSet = PriorityQueue<Node> {o1, o2 ->
        when {
            fScore[o1] ?: Double.MAX_VALUE < fScore[o2] ?: Double.MAX_VALUE -> -1
            fScore[o2] ?: Double.MAX_VALUE < fScore[o1] ?: Double.MAX_VALUE -> 1
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
            if (closedSet.contains(neighbor) || exclude(neighbor)) {
                continue // Ignore the neighbor which is already evaluated or shouldn't be.
            }

            val tentativeGScore = gScore[current]!! + cost(current, neighbor) // Cost of this path.
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

private fun <Node> reconstructPath(
    cameFrom: Map<Node, Node>,
    current: Node?
): List<Node> {
    var cur = current
    val totalPath = ArrayList<Node>()

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
