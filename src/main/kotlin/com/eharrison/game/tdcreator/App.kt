package com.eharrison.game.tdcreator

import javafx.animation.Interpolator
import javafx.animation.RotateTransition
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.scene.*
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.CheckBox
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.*
import javafx.scene.transform.Rotate
import javafx.stage.Stage
import javafx.util.Duration
import tornadofx.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.sqrt
import kotlin.system.measureNanoTime


fun main(args: Array<String>) {
    launch<MyApp>(args)
}

private val game = createGame()

class MyApp: App(MasterView::class) {
    init {
        importStylesheet(MyStyle::class)
    }

    override fun start(stage: Stage) {
        texture = Image(textureLoc)
        texturedMaterial.diffuseMap = texture

        val group: Group = render3D(game)

        val rotate: RotateTransition = rotate3dGroup(group)

        val layout = VBox(
            createControls(rotate),
            createScene3D(group)
        )

        stage.title = "Model Viewer"

        val scene = Scene(layout, Color.CORNSILK)
        stage.scene = scene
        stage.show()

        val startingState = game
        val dst = 1.0 / 60.0 // Integrate 60 times per second
        val drt = 1.0 //1.0 / 30.0 // Render 30 times per second
        val mit = 0.25 // Maximum integration time is 1/4 of a second

        Thread {
            var count = 0
            loop(running::get, paused::get, dst, drt, mit, startingState, ::input, ::integrate2, ::interpolate) {
                println(render(game))
                if (count++ == 14) {
                    currentInput.set(Input(paused = false, shutdown = true))
                }
            }
        }.start()
    }

//    override fun start(stage: Stage) {
//        stage.minHeight = 400.0
//        stage.minWidth = 400.0
//        super.start(stage)
//        println("Started")
//
//        val controller = FX.find(MyController::class.java)
//        println(controller.c)
//
//        controller.c.fill = Color.YELLOW
//        controller.c.fillRect(10.0,10.0,100.0,100.0)
//    }
}





fun createGame(): Game {
    val game = Game(10,10)

//    addTower(game, Tower(Region(0,0)))
//    addTower(game, Tower(Region(9,9)))

    addTower(game, Tower(Region(4,4)))
    addTower(game, Tower(Region(4,5)))
    addTower(game, Tower(Region(5,4)))
    addTower(game, Tower(Region(3,6)))

    addCreep(game, Creep(PointProperty(0.5,0.5)))
    addCreep(game, Creep(PointProperty(2.5,0.5)))
    addCreep(game, Creep(PointProperty(0.5,2.5)))

    return game
}

fun render3D(game: Game): Group {
    val group = Group()

    group.add(getGameBox(game))
    group.addAll(game.towers.map { getTowerBox(it) })
    group.addAll(game.creeps.map { getCreepBox(it) })

    group.scaleX = 40.0
    group.scaleY = 40.0
    group.scaleZ = 40.0
    group.translateX = VIEWPORT_SIZE / 2 + MODEL_X_OFFSET
    group.translateY = VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET
    group.translateZ = VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET
    return group
}

fun Group.addAll(nodes: Iterable<javafx.scene.Node>) = nodes.forEach { add(it) }

val boardMaterial = PhongMaterial(Color.GREEN)
val towerMaterial = PhongMaterial(Color.BLUE)
val creepMaterial = PhongMaterial(Color.RED)

fun getGameBox(game: Game): Box {
    val width = game.region.maxX - game.region.minX + 1.0
    val height = game.region.maxY - game.region.minY + 1.0
    val depth = game.region.maxZ - game.region.minZ + 1.0
    val box = Box(width, height, 0.5)
    box.translateX = box.width / 2.0
    box.translateY = box.height / 2.0
    box.translateZ = box.depth / 2.0
    box.material = boardMaterial

    require(box.boundsInParent.minX == game.region.minX.toDouble())
    require(box.boundsInParent.minY == game.region.minY.toDouble())
    require(box.boundsInParent.minZ == 0.0)
    require(box.boundsInParent.maxX == game.region.maxX + 1.0)
    require(box.boundsInParent.maxY == game.region.maxY + 1.0)
    require(box.boundsInParent.maxZ == 0.5)

    return box
}

fun getTowerBox(tower: Tower): Box {
    val width = tower.region.maxX - tower.region.minX + 1.0
    val height = tower.region.maxY - tower.region.minY + 1.0
    val depth = tower.region.maxZ - tower.region.minZ + 1.0
    val box = Box(width, height, depth)
    box.translateX = tower.region.minX + 0.5
    box.translateY = tower.region.minY + 0.5
    box.translateZ = -(tower.region.minZ + 0.5)
    box.material = towerMaterial

    require(box.boundsInParent.minX == tower.region.minX.toDouble())
    require(box.boundsInParent.minY == tower.region.minY.toDouble())
    require(box.boundsInParent.minZ == -(tower.region.maxZ + 1.0))
    require(box.boundsInParent.maxX == tower.region.maxX + 1.0)
    require(box.boundsInParent.maxY == tower.region.maxY + 1.0)
    require(box.boundsInParent.maxZ == -tower.region.maxZ.toDouble())

    return box
}

fun getCreepBox(creep: Creep): Sphere {
    val sphere = Sphere(0.25)
    sphere.translateXProperty().bind(creep.location.xProperty)
    sphere.translateYProperty().bind(creep.location.yProperty)
    sphere.translateZProperty().bind(creep.location.zProperty)
    sphere.material = creepMaterial

    require(sphere.boundsInParent.centerX == creep.location.x)
    require(sphere.boundsInParent.centerY == creep.location.y)
    require(sphere.boundsInParent.centerZ == creep.location.z)

    return sphere
}

private fun integrate2(input: Input, state: Game, t: Double, dt: Double): Game {
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
                creep.location.add(d)
            }
        }
    }
//    println("integrate took ${ns / 1000000L} milliseconds")

    return state
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



private const val VIEWPORT_SIZE = 800
private const val MODEL_SCALE_FACTOR = 40.0
private const val MODEL_X_OFFSET = 0.0
private const val MODEL_Y_OFFSET = 0.0
private const val MODEL_Z_OFFSET = VIEWPORT_SIZE / 2.toDouble()

private const val textureLoc = "file:src/main/resources/marble.jpg"

private var texture: Image? = null
private val texturedMaterial: PhongMaterial = PhongMaterial()

private val meshView: MeshView = loadMeshView()

private fun loadMeshView(): MeshView {
    val points = floatArrayOf(-5f, 5f, 0f, -5f, -5f, 0f, 5f, 5f, 0f, 5f, -5f, 0f)
    val texCoords = floatArrayOf(1f, 1f, 1f, 0f, 0f, 1f, 0f, 0f)
    val faces = intArrayOf(2, 2, 1, 1, 0, 0, 2, 2, 3, 3, 1, 1)
    val mesh = TriangleMesh()
    mesh.points.setAll(*points)
    mesh.texCoords.setAll(*texCoords)
    mesh.faces.setAll(*faces)
    return MeshView(mesh)
}

private fun buildScene(): Group {
    meshView.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET)
    meshView.setTranslateY(VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET)
    meshView.setTranslateZ(VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET)
    meshView.setScaleX(MODEL_SCALE_FACTOR)
    meshView.setScaleY(MODEL_SCALE_FACTOR)
    meshView.setScaleZ(MODEL_SCALE_FACTOR)

    val box = Box(100.0, 100.0, 100.0)
    box.translateX = VIEWPORT_SIZE / 2.0
    box.translateY = VIEWPORT_SIZE / 2.0
    box.translateZ = VIEWPORT_SIZE / 2.0

    return Group(box)
}

private fun createScene3D(group: Group): SubScene {
    val scene3d = SubScene(group, VIEWPORT_SIZE.toDouble(), VIEWPORT_SIZE * 9.0 / 16, true, SceneAntialiasing.BALANCED)
    scene3d.fill = Color.rgb(10, 10, 40)
    scene3d.camera = PerspectiveCamera()
    return scene3d
}

private fun createControls(rotateTransition: RotateTransition): VBox {
    val cull = CheckBox("Cull Back")
    meshView.cullFaceProperty().bind(
        Bindings.`when`(
            cull.selectedProperty()
        )
            .then(CullFace.BACK)
            .otherwise(CullFace.NONE)
    )
    val wireframe = CheckBox("Wireframe")
    meshView.drawModeProperty().bind(
        Bindings.`when`(
            wireframe.selectedProperty()
        )
            .then(DrawMode.LINE)
            .otherwise(DrawMode.FILL)
    )
    val rotate = CheckBox("Rotate")
    rotate.selectedProperty().addListener { _ ->
        if (rotate.isSelected) {
            rotateTransition.play()
        } else {
            rotateTransition.pause()
        }
    }
    val texture = CheckBox("Texture")
    meshView.materialProperty().bind(
        Bindings.`when`(
            texture.selectedProperty()
        )
            .then(texturedMaterial)
            .otherwise(null as PhongMaterial?)
    )
    val controls = VBox(10.0, rotate, texture, cull, wireframe)
    controls.padding = Insets(10.0)
    return controls
}

private fun rotate3dGroup(group: Group): RotateTransition {
    val rotate = RotateTransition(Duration.seconds(10.0), group)
    rotate.axis = Rotate.Y_AXIS
    rotate.fromAngle = 0.0
    rotate.toAngle = 360.0
    rotate.interpolator = Interpolator.LINEAR
    rotate.cycleCount = RotateTransition.INDEFINITE
    return rotate
}








private val running = AtomicBoolean(true)
private val paused = AtomicBoolean(false)
private val currentInput = AtomicReference<Input>(Input(paused = false, shutdown = false))

//private val game = Game(10,10)
//
//fun main() {
//    val dst = 1.0 / 60.0 // Integrate 60 times per second
//    val drt = 1.0 //1.0 / 30.0 // Render 30 times per second
//    val mit = 0.25 // Maximum integration time is 1/4 of a second
//    val startingState = game
//
//    addTower(game, Tower(Region(4,4)))
//    addTower(game, Tower(Region(4,5)))
//    addTower(game, Tower(Region(5,4)))
//    addTower(game, Tower(Region(3,6)))
//
//    addCreep(startingState, Creep(Point(0.5,0.5)))
//    addCreep(startingState, Creep(Point(2.5,0.5)))
//    addCreep(startingState, Creep(Point(0.5,2.5)))
//
//    println(render(game))
//
//    var count = 0
//    loop(running::get, paused::get, dst, drt, mit, startingState, ::input, ::integrate, ::interpolate) {
//        println(render(game))
//        if (count++ == 14) {
//            currentInput.set(Input(paused = false, shutdown = true))
//        }
//    }
//
//    println(game)
//}

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

//private fun integrate(input: Input, state: Game, t: Double, dt: Double): Game {
//    if (input.shutdown) running.set(false)
//
//    val ns = measureNanoTime {
//        for (creep in state.creeps) {
//            val loc = creep.location
//            val start = Node(loc.x.minus(0.5).toInt(), loc.y.minus(0.5).toInt(), loc.z.toInt())
//            val end = Node(9, 9, loc.z.toInt())
//
//            val path = aStar(start, end, ::getNeighbors, ::distance, blocked, euclidean)
//            if (path.isNotEmpty()) {
//                val vector = Point(path[0].x + 0.5, path[0].y + 0.5, path[0].z.toDouble()) - loc
//                val d = vector.toUnitVector() * dt
//                creep.location = loc + d
//            }
//        }
//    }
////    println("integrate took ${ns / 1000000L} milliseconds")
//
//    return state
//}

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
    return if (node0.x != node1.x && node0.y != node1.y) sqrt(2.0) else 1.0
}

private val blocked: (Node) -> Boolean = { _ -> false }

private val euclidean: (Node, Node) -> Double = { node0, node1 ->
    val x = node1.x - node0.x
    val y = node1.y - node0.y
    sqrt((x * x + y * y).toDouble())
}