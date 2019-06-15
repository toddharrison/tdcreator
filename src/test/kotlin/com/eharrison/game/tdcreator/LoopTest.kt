package com.eharrison.game.tdcreator

import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private val running = AtomicBoolean(true)
private val paused = AtomicBoolean(false)
private val currentInput = AtomicReference<Input>(Input(paused = false, shutdown = false))

class LoopTest {
    @Test
    fun `run the game loop for 10 renderings`() {
        val dst = 1.0 / 60.0 // Integrate 60 times per second
        val drt = 1.0 / 30.0 // Render 30 times per second
        val mit = 0.25 // Maximum integration time is 1/4 of a second
        val startingState = State(0.0,0.0) // Starting location

        var count = 0
        loop(running::get, paused::get, dst, drt, mit, startingState, ::input, ::integrate, ::interpolate) {
            // This is what is run 30 times a second to render the results of the new State
            count++
            println("$count: $it")
            if (count == 10) currentInput.set(Input(paused = false, shutdown = true))
        }
    }
}

data class Input(
    val paused: Boolean,
    val shutdown: Boolean
)

data class State(
    val x: Double = 0.0,
    val y: Double = 0.0
)

private fun input(): Input {
    // Get input into the simulation (user, event, system, etc.)
    return currentInput.get()
}

private fun integrate(input: Input, state: State, t: Double, dt: Double): State {
    // Update to new game state from input and change in time
    if (input.shutdown) running.set(false)
    return state.copy(x = t)
}

private fun interpolate(startState: State, startWeight: Double, endState: State, endWeight: Double): State {
    // Merge startState and endState (linear interpolation using endWeight, perhaps?)
    return endState
}
