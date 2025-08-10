package io.github.gearup12499.taskshark
import io.github.gearup12499.taskshark.prefabs.VirtualGroup

interface ITask<Self: ITask<Self>> {
    companion object {
        val COMPARE_PRIORITY: Comparator<ITask<*>> = Comparator.comparing(ITask<*>::getPriority).thenComparing(ITask<*>::hashCode)
    }

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
     *
     * ## Usually not used directly; instead use [Scheduler.add].
     *
     * Any implementation MUST call [register][Scheduler.register] on the provided [parent] and update the value
     * returned by [getId] accordingly.
     *
     * Only valid when the task has not started and prior to any calls to [canStart].
     *
     * @suppress
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
     * Priority hints which tasks should be processed first.
     * High priority tasks are processed earlier than low priority tasks, in general.
     *
     * Start/acquire order is **not guaranteed** with this method, due to optimizations in the scheduler.
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
     * The task will be in the [Finishing][ITask.State.Finishing] state throughout this callback.
     *
     * **If the task finished normally** (or [stop]ped with `cancel = false`, or [finish]ed), [completedNormally] will be `true`.
     *
     * **If the task was cancelled**, [completedNormally] will be `false`.
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
    fun dependedLocks(): Set<Lock> = emptySet()

    /**
     * Returns the set of [ITask]s that need to be completed for this task to begin.
     *
     * **If any of these tasks are [Cancelled][State.Cancelled]**, this task is also cancelled.
     *
     * The converse of this relationship can be found in [getDependents], though that method
     * functions mainly as a hint to wake up tasks. Schedulers may make these hints mandatory for
     * correct operation.
     *
     * @return unordered [Set] of [ITask]s that must be completed before the task can begin
     */
    fun dependedTasks(): Set<ITask<*>> = emptySet()

    /**
     * Provides hints for quicker startup times on chains of tasks.
     *
     * Optimizing [Scheduler]s may utilize this to only 'wake up' queued tasks when their
     * depended on tasks are completed; returning too much is always better than not enough.
     *
     * @return a list of tasks that might be waiting for this task to finish. Their requirements will be
     * checked when this task finishes, and may start within the same tick.
     */
    fun getDependents(): Set<ITask<*>>

    /**
     * Stops this task, moving it to an appropriate final state and potentially calling [onFinish].
     *
     * See [canStart], [onStart], [onTick], and [onFinish] documentation for behavior details.
     *
     * @param cancel if `true` or omitted, the task is cancelled; if `false`, the task completed normally
     *
     * If this task is currently being processed, then this function **throws [TaskStopException]**.
     */
    @Throws(TaskStopException::class)
    fun stop(cancel: Boolean)

    /**
     * Stops this task, moving it the [Cancelled][ITask.State.Cancelled] state and potentially calling [onFinish].
     *
     * See [canStart], [onStart], [onTick], and [onFinish] documentation for behavior details.
     *
     * If this task is currently being processed, then this function **throws [TaskStopException]**.
     */
    @Throws(TaskStopException::class)
    fun stop() = stop(true)
    
    /**
     * Stops this task, moving it the [Finished][ITask.State.Finished] state and potentially calling [onFinish].
     *
     * See [canStart], [onStart], [onTick], and [onFinish] documentation for behavior details.
     *
     * If this task is currently being processed, then this function **throws [TaskStopException]**.
     */
    @Throws(TaskStopException::class)
    fun finish() = stop(false)

    /**
     * Sets up the provided [other] task to run after this task completes, adding it to the dependency tree.
     *
     * @return other, for chaining
     */
    fun <T: ITask<T>> then(other: T): T

    /**
     * Add the provided [before] task as a dependency for this task.
     */
    fun require(before: ITask<*>): Self
    /**
     * Add the provided [lock] lock as a dependency for this task.
     */
    fun require(lock: Lock): Self

    /**
     * If true, allows breaking the idea that this task needs to be added to a scheduler to be registered correctly.
     *
     * Used (for example) in [VirtualGroup], which isn't really a task but
     * acts like one for programmer QOL reasons.
     *
     * @suppress
     */
    fun isVirtual(): Boolean = false

    fun describeVerbose() = buildString {
        append("<")
        append(this@ITask::class.simpleName ?: "[unknown class]")
        append(" #")
        append(getId())
        append(">")
    }

    class IllegalTransitionException(subject: ITask<*>, from: State, to: State) : RuntimeException(
        "Can't transition task ${subject.describeVerbose()} from $from (order ${from.order}) to $to (order ${to.order})"
    )
}
