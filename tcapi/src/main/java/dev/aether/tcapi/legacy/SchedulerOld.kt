package dev.aether.tcapi.legacy

abstract class SchedulerOld {

    /// Build and add a new Task.
    abstract fun task(configure: Task.() -> Unit): Task

    /// Add a new ITask to this scheduler.
    abstract fun <T : ITaskOld> add(t: T): T

    /// Query if a resource is being used.
    open fun isResourceInUse(resource: SharedResource): Boolean = false

    /// Acquires this lock if there is no task holding it.
    abstract fun manualAcquire(resource: SharedResource): Boolean
    /// Releases a manually-held lock.
    abstract fun manualRelease(resource: SharedResource)

    /// Run one tick.
    abstract fun tick()

    /// Get the number of ticks that have occurred.
    abstract fun getTicks(): Int

    /// Registers a task with this scheduler.
    internal abstract fun register(task: ITaskOld): Int

    /// The next task ID, which will be used for the next task.
    abstract val nextId: Int

    /// Stops all tasks.
    abstract fun panic()

    abstract fun filteredStop(predicate: (ITaskOld) -> Boolean, cancel: Boolean)
    abstract fun filteredStop(predicate: (ITaskOld) -> Boolean)
    abstract fun filteredStop(
        predicate: (ITaskOld) -> Boolean,
        cancel: Boolean,
        dropNonStarted: Boolean
    )

    abstract fun taskCount(): Int
}