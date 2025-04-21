package dev.aether.tcapi.legacy

import java.util.function.Consumer

class TaskGroupScheduler(internal val outerRequirements: MutableSet<SharedResource>) : MultitaskScheduler() {
    internal var errorOnNewLockAcquire = false
    val subtaskRequirements: MutableSet<SharedResource> = mutableSetOf()

    override fun register(task: ITaskOld): Int {
        val toAdd = task.requirements() - (outerRequirements + subtaskRequirements)
        if (!toAdd.isEmpty() && errorOnNewLockAcquire) throw IllegalStateException("Too late to add additional requirements (already committed) - try using extraDepends to specify dependencies earlier")
        subtaskRequirements.addAll(toAdd)
        return super.register(task)
    }
}

open class TaskGroup(outerScheduler: SchedulerOld) : TaskTemplate(outerScheduler) {
    protected val extraDeps: MutableSet<SharedResource> = mutableSetOf()
    protected val innerScheduler = TaskGroupScheduler(extraDeps)

    /**
     * Access the inner scheduler, cast to a generic Scheduler. Not recommended for general use.
     */
    fun accessInner() = innerScheduler as SchedulerOld

    fun with(provider: (SchedulerOld) -> Unit): TaskGroup {
        provider(innerScheduler)
        return this
    }

    fun with(provider: Consumer<SchedulerOld>): TaskGroup {
        provider.accept(innerScheduler)
        return this
    }

    fun extraDepends(vararg with: SharedResource): TaskGroup {
        extraDeps.addAll(with)
        return this
    }

    fun extraClear() = extraDeps.clear()

    override fun requirements(): Set<SharedResource> {
        return innerScheduler.subtaskRequirements + extraDeps
    }

    override fun invokeOnStart() {
        super.invokeOnStart()
        // If we try to grab more stuff, the list of requirements could change, resulting in a 'non-owned free' error
        innerScheduler.errorOnNewLockAcquire = true
    }

    override fun invokeOnTick() {
        innerScheduler.tick()
    }

    override fun invokeOnFinish() {
        // run onFinish handlers for inner tasks
        innerScheduler.filteredStop { true }
    }

    override fun invokeIsCompleted(): Boolean {
        return !innerScheduler.hasJobs()
    }

    private val genName: String; get() = "(a group containing ${innerScheduler.taskCount()} subtasks)"
    private var customName = "unnamed"
    override var name: String
        get() = "$customName $genName"
        set(value) {
            customName = value
        }

    override fun display(indent: Int, write: (String) -> Unit) {
        innerScheduler.displayStatus(false, false, write, indent)
    }
}