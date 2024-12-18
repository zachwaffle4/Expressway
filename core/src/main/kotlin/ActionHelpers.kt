@file:JvmName("ActionHelpers")
package page.j5155.actionAdditions

import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.now

// Released under the MIT License and the BSD-3-Clause license by contributors (you may use it under either one)
// Contributors: j5155, Zach.Waffle

abstract class InitLoopAction : Action {
    private var initialized = false

    /**
     * Initializes the action.
     * This will always run before [loop].
     */
    abstract fun init()

    /**
     * Contents of the action.
     * @return whether to continue with the action; true to continue looping, false to end (like a standard Action)
     */
    abstract fun loop(t: TelemetryPacket): Boolean

    final override fun run(t: TelemetryPacket): Boolean { // final to prevent downstream classes from overriding it
        if (!initialized) {
            init()
            initialized = true
        }
        return loop(t)
    }
    // intentionally not overriding preview
}

/**
 * Allows for the creation of an Action using init and loop methods and a boolean-returning end condition.
 * Like InitLoopAction, but with the end condition seperated out
 * @param condition stops action when condition is *true* (opposite of a standard Action)
 */
abstract class InitLoopCondAction protected constructor(val condition: () -> Boolean) : Action {
    /**
     * Initializes the action.
     * This will always run before [loop].
     */
    abstract fun init()

    /**
     * Contents of the action.
     * This will repeat *until* [condition] is true (opposite of a standard Action).
     */
    abstract fun loop(p: TelemetryPacket)

    private var hasInit = false

    final override fun run(p: TelemetryPacket): Boolean {
        if (!hasInit) {
            init()
            hasInit = true
        }
        loop(p)
        return !condition.invoke()
    }
}

/**
 * Takes any number of actions as input and runs them all in parallel; force-stops them all as soon as one ends
 *
 * Useful for running PID update actions in parallel with trajectories
 * @param actions actions to run in parallel
 */
open class RaceParallelAction(vararg val actions: Action) : Action {
    override fun run(p: TelemetryPacket): Boolean {
        var finished = true
        for (action in actions) finished = finished && action.run(p)
        return finished
    }

    override fun preview(fieldOverlay: Canvas) {
        for (action in actions) action.preview(fieldOverlay)
    }
}

/**
 * Runs an input action in parallel with an update function
 */
class ActionWithUpdate(val action: Action, val func: Runnable) : Action by action {
    override fun run(p: TelemetryPacket): Boolean {
        func.run()
        return action.run(p)
    }
}

/**
 * Runs an action with a configurable timeout in seconds, defaulting to 5 seconds.
 *
 * After the timeout expires, the action will be FORCIBLY stopped.
 * Ensure that this will not leave your robot in a dangerous state (motors still moving, etc.) that is not resolved by another action.
 */
class TimeoutAction(val action: Action, val timeout: Double = 5.0) : Action by action {
    private var startTime = 0.0

    override fun run(p: TelemetryPacket): Boolean {
        if (startTime == 0.0) startTime = now()
        if (now() - startTime > timeout) return false
        return action.run(p)
    }
}

/** Takes two actions and a condition supplier as inputs.
 * Runs the first action if the [determinant] is true and runs the second action if it is false.
 *
 * @param trueAction action to run if [determinant] is true
 * @param falseAction action to run if [determinant] is false
 * @param determinant condition that determines which action will be run
 */
class ConditionalAction(val trueAction: Action, val falseAction: Action, val determinant: () -> Boolean) :
    InitLoopAction() {
    private lateinit var chosenAction: Action

    override fun init() {
        chosenAction =
            if (determinant.invoke()) { // use .invoke() to check the value of the condition by running the input function
                trueAction // and then save the decision to the chosenAction variable
            } else {
                falseAction
            }
    }

    override fun loop(p: TelemetryPacket): Boolean {
        return chosenAction.run(p)
    }

    // ambiguous which one to preview, so preview both
    override fun preview(fieldOverlay: Canvas) {
        trueAction.preview(fieldOverlay)
        falseAction.preview(fieldOverlay)
    }
}

