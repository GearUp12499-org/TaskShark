package io.github.gearup12499.taskshark

interface ITask {
    enum class State(val order: Int) {
        NotStarted(0),
        Starting(1),
        Ticking(2),
        Finishing(3),
        Finished(4),
        Cancelled(4),
    }

    /**
     * Returns the current [State][ITask.State] of the Task.
     *
     * When constructed, a new Task must begin in the [NotStarted][State.NotStarted] state.
     *
     * @return the current state of this Task.
     */
    fun getState(): State

    /**
     * Updates the current [State][ITask.State] of the Task.
     *
     * Moving to a state with lower [order][ITask.State.order] is generally not permitted.
     * (For example, moving from [Finished][ITask.State.Finished] to [Ticking][ITask.State.Ticking] is not allowed.)
     *
     * @param newState the target state to move to, handling events as needed.
     */
    @Throws(IllegalTransitionException::class)
    fun transition(newState: State)

    /**
     * Registers this task to a Scheduler.
     * Any implementation MUST call [register][Scheduler.register] on the provided [parent] and update the value
     * returned by [getId] accordingly.
     *
     * Only valid when the task has not started and prior to any calls to [canStart].
     *
     * @param parent Scheduler that this task is now managed by.
     */
    fun register(parent: Scheduler)

    /**
     * Return the task's ID number in the scheduler it's registered to.
     *
     * If not associated with any scheduler, the result should be `-1`.
     *
     * @return This task's ID number in its registered scheduler.
     */
    fun getId(): Int

    /**
     * Priority determines which tasks should be processed first.
     * High priority tasks are processed earlier than low priority tasks.
     */
    fun getPriority(): Int

    /**
     * Returns a set of descriptive tags that apply to this task.
     *
     * These tags can be used for any purpose.
     * They are generally useful for selecting and filtering tasks.
     */
    fun getTags(): Set<String>

    /**
     * **Implement/override this method** to check custom conditions upon which the Task can begin ticking.
     *
     * Lock-related mechanisms are handled separately, and this function may not be called every tick if
     * other pre-requisites are not met.
     *
     * **Cancellation** in this stage does not invoke [onFinish].
     * If the task is cancelled before having started, it is moved directly to the [Cancelled][ITask.State.Cancelled] state.
     *
     * @return `true` if the custom start conditions are met, and `false` otherwise. Must return `true` for the task to start.
     */
    fun canStart(): Boolean

    /**
     * **Implement/override this method** to run code when the Task starts for the first time.
     *
     * The task will be in the [Starting][ITask.State.Starting] state throughout this callback.
     *
     * **Cancellation** in this stage invokes [onFinish].
     */
    fun onStart()

    /**
     * **Implement/override this method** to run code repeatedly during the Task's lifetime.
     *
     * The task will be in the [Ticking][ITask.State.Ticking] state throughout this callback.
     * Ticking tasks remain in the Ticking state until they return `true` (to indicate completion)
     * or are cancelled.
     *
     * **Cancellation** in this stage invokes [onFinish].
     *
     * @return `true` to end the task successfully, or else `false` to continue ticking.
     */
    fun onTick(): Boolean

    /**
     * **Implement/override this method** to handle cleaning up a Task after successful completion
     * or cancellation.
     *
     * **If the task finished normally**, the task will be in the [Finishing][ITask.State.Finishing] state
     * throughout this callback, and [completedNormally] will be `true`.
     *
     * **If the task was cancelled**, the task will be in whichever state it was in when it was cancelled,
     * and [completedNormally] will be `false`.
     *
     * **Cancellation** in this stage moves the task immediately to the [Cancelled][ITask.State.Cancelled] state, and execution in the [onFinish] block will end, even if the task would have otherwise finished normally.
     *
     * @param completedNormally `true` for normal completion of the Task, and `false` for cancellation.
     */
    fun onFinish(completedNormally: Boolean)

    /**
     * Returns the set of [Lock]s required for this task to begin. These are checked *before*
     * [canStart].
     *
     * All of these have to be free simultaneously for the task to begin.
     *
     * @return unordered [Set] of [Lock]s that must be free before the task can begin
     */
    fun getRequirements(): Set<Lock>

    /**
     * Provides hints for quicker startup times on chains of tasks.
     * Nothing will break if you just return an empty `Set`, but inter-task switches
     * might take a bit longer.
     *
     * @return a list of tasks that might be waiting for this task to finish. Their requirements will be
     * checked when this task finishes, and may start within the same tick.
     */
    fun getDependents(): Set<ITask>

    /**
     * Stops this task, moving it to an appropriate final state and potentially calling [onFinish].
     *
     * See [canStart], [onStart], [onTick], and [onFinish] documentation for behavior details.
     *
     * @param cancel if `true` or omitted, the task is cancelled; if `false`, the task completed normally
     *
     * If the [getProcessing] [Signal] is active, then this function **throws [TaskStopException]**.
     */
    fun stop(cancel: Boolean)

    /**
     * Stops this task, moving it the [Cancelled][ITask.State.Cancelled] state and potentially calling [onFinish].
     *
     * See [canStart], [onStart], [onTick], and [onFinish] documentation for behavior details.
     *
     * If the [getProcessing] [Signal] is active, then this function **throws [TaskStopException]**.
     */
    fun stop() = stop(true)
    
    /**
     * Stops this task, moving it the [Finished][ITask.State.Finished] state and potentially calling [onFinish].
     *
     * See [canStart], [onStart], [onTick], and [onFinish] documentation for behavior details.
     *
     * If the [getProcessing] [Signal] is active, then this function **throws [TaskStopException]**.
     */
    fun finish() = stop(false)

    /**
     * This [Signal] is active when event handler code is running.
     *
     * If this signal is [active][Signal.isActive], then:
     * * throwing [TaskStopException] returns control to the scheduler and stops the task
     */
    fun getProcessing(): Signal

    fun <T: ITask> then(other: T): T

    fun describeVerbose() = buildString {
        append("<")
        append(this@ITask::class.simpleName ?: "[unknown class]")
        append(" #")
        append(getId())
        append(">")
    }

    class IllegalTransitionException(subject: ITask, from: State, to: State) : RuntimeException(
        "Can't transition task ${subject.describeVerbose()} from $from (order ${from.order}) to $to (order ${to.order})"
    )
}
