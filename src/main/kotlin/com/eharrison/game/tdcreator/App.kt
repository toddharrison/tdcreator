package com.eharrison.game.tdcreator

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.measureNanoTime
import tornadofx.*
import tornadofx.FX



fun main(args: Array<String>) {
    launch<MyApp>(args)
}

class MyApp: App(MasterView::class) {
    init {
        importStylesheet(MyStyle::class)
    }

    override fun start(stage: Stage) {
        stage.minHeight = 400.0
        stage.minWidth = 400.0
        super.start(stage)
        println("Started")

        val controller = FX.find(MyController::class.java)
        println(controller.c)

        controller.c.fill = Color.YELLOW
        controller.c.fillRect(10.0,10.0,100.0,100.0)
    }
}

class MyStyle: Stylesheet() {
}

class MasterView: View() {
    override val root = borderpane {
        top<TopView>()
        center<MyView>()
        bottom<BottomView>()
    }
}

class TopView: View() {
    override val root = label("Top View")
}

class BottomView: View() {
    override val root = label("Bottom View")
}

class MyView: View() {
    val controller: MyController by inject()
    var c: Canvas by singleAssign()

    override val root = stackpane {
        style {
            backgroundColor += Color.RED
        }
//        minHeight = 400.0
//        minWidth = 400.0
        controller.c = canvas {
            height = 400.0
            width = 400.0
            scaleX = 1.0
            scaleY = 1.0
            scaleZ = 1.0
        }.graphicsContext2D
        group {
            rectangle {
                fill = Color.BLUE
                width = 300.0
                height = 150.0
                arcWidth = 20.0
                arcHeight = 20.0
            }
        }
    }
}

class MyController: Controller() {
    var c: GraphicsContext by singleAssign()
}










private val running = AtomicBoolean(true)
private val paused = AtomicBoolean(false)
private val currentInput = AtomicReference<Input>(Input(paused = false, shutdown = false))

private val game = Game(10,10)

fun main() {
    val dst = 1.0 / 60.0 // Integrate 60 times per second
    val drt = 1.0 //1.0 / 30.0 // Render 30 times per second
    val mit = 0.25 // Maximum integration time is 1/4 of a second
    val startingState = game

    addTower(game, Tower(Region(4,4)))
    addTower(game, Tower(Region(4,5)))
    addTower(game, Tower(Region(5,4)))
    addTower(game, Tower(Region(3,6)))

    addCreep(startingState, Creep(Point(0.5,0.5)))
    addCreep(startingState, Creep(Point(2.5,0.5)))
    addCreep(startingState, Creep(Point(0.5,2.5)))

    println(render(game))

    var count = 0
    loop(running::get, paused::get, dst, drt, mit, startingState, ::input, ::integrate, ::interpolate) {
        println(render(game))
        if (count++ == 14) {
            currentInput.set(Input(paused = false, shutdown = true))
        }
    }

    println(game)
}

data class Input(
    val paused: Boolean,
    val shutdown: Boolean
)

data class Node(
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0
)

private fun input(): Input {
    return currentInput.get()
}

private fun integrate(input: Input, state: Game, t: Double, dt: Double): Game {
    if (input.shutdown) running.set(false)

    val ns = measureNanoTime {
        for (creep in state.creeps) {
            val loc = creep.location
            val start = Node(loc.x.minus(0.5).toInt(), loc.y.minus(0.5).toInt(), loc.z.toInt())
            val end = Node(9, 9, loc.z.toInt())

            val path = aStar(start, end, ::getNeighbors, ::distance, blocked, euclidean)
            if (path.isNotEmpty()) {
                val vector = Point(path[0].x + 0.5, path[0].y + 0.5, path[0].z.toDouble()) - loc
                val d = vector.toUnitVector() * dt
                creep.location = loc + d
            }
        }
    }
//    println("integrate took ${ns / 1000000L} milliseconds")

    return state
}

private fun interpolate(startState: Game, startWeight: Double, endState: Game, endWeight: Double): Game {
    return endState
}

private fun getNeighbors(node: Node): List<Node> {
    val neighbors = mutableListOf<Node>()
    for (y in -1..1) {
        for (x in -1..1) {
            if (x == 0 && y == 0) continue

            val nodeX = node.x + x
            val nodeY = node.y + y
            if (
                nodeX in 0..game.region.maxX
                && nodeY in 0..game.region.maxY
                && getTowersAt(game, Region(nodeX, nodeY, node.z)).isEmpty()
            ) {
                val neighbor = Node(nodeX, nodeY, node.z)
                neighbors.add(neighbor)
            }
        }
    }
    return neighbors
}

private fun distance(node0: Node, node1: Node): Double {
    return if (node0.x != node1.x && node0.y != node1.y) Math.sqrt(2.0) else 1.0
}

private val blocked: (Node) -> Boolean = { _ -> false }

private val euclidean: (Node, Node) -> Double = { node0, node1 ->
    val x = node1.x - node0.x
    val y = node1.y - node0.y
    Math.sqrt((x * x + y * y).toDouble())
}