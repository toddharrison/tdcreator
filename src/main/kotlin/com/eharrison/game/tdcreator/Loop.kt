package com.eharrison.game.tdcreator

// https://gafferongames.com/post/fix_your_timestep/

typealias LoopCheck = () -> Boolean
typealias LoopInput<In> = () -> In
typealias LoopIntegrate<In, State> = (In, State, Double, Double) -> State
typealias LoopInterpolate<State> = (State, Double, State, Double) -> State
typealias LoopRender<State> = (State) -> Unit

fun <In, State> loop(
    running: LoopCheck,
    paused: LoopCheck,
    dst: Double,
    drt: Double,
    mit: Double, // max integration time
    startingState: State,
    input: LoopInput<In>,
    integrate: LoopIntegrate<In, State>,
    interpolate: LoopInterpolate<State>,
    render: LoopRender<State>
) {
    require(dst > 0.0)
    require(drt >= 0.0)

    var previousState = startingState
    var currentState = startingState

    var t = 0.0

    var currentTime = System.nanoTime()
    var stateAccumulator = 0.0
    var renderAccumulator = 0.0

    while (running()) {
        if (!paused()) {
            val newTime = System.nanoTime()
            val frameTime = Math.min((newTime - currentTime).times(1.0e-9), mit)
            currentTime = newTime

            stateAccumulator += frameTime
            renderAccumulator += frameTime

            if (stateAccumulator >= dst) {
                val i = input()
                while (stateAccumulator >= dst) {
                    previousState = currentState
                    currentState = integrate(i, currentState, t, dst)
                    t += dst
                    stateAccumulator -= dst
                }
            }

            if (drt > 0.0) {
                while (renderAccumulator >= drt) {
                    val blend = stateAccumulator / dst
                    val state = interpolate(previousState, 1.0 - blend, currentState, blend)
                    render(state)
                    renderAccumulator -= drt
                }
            } else {
                val blend = stateAccumulator / dst
                val state = interpolate(previousState, 1.0 - blend, currentState, blend)
                render(state)
            }
        }
    }
}

