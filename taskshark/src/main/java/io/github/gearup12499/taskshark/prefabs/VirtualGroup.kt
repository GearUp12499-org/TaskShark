package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Lock
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.api.BuiltInTags
import io.github.gearup12499.taskshark.virtual.TaskBin

class VirtualGroup(configure: Configure) : ITask<VirtualGroup> {
    private var scheduler: Scheduler = TaskBin()
    val inside: MutableSet<ITask<*>> = mutableSetOf()

    fun interface Configure {
        fun VirtualGroupDsl.conf()
    }

    @DslMarker
    annotation class VirtualGroupDslMarker

    @VirtualGroupDslMarker
    inner class VirtualGroupDsl {
        fun <T: ITask<*>> add(task: T): T {
            inside.add(task)
            scheduler.add(task)
            return task
        }
    }

    init {
        with(configure) {
            VirtualGroupDsl().conf()
        }
    }

    private fun reject(): Nothing = throw UnsupportedOperationException("This operation is not available on $this.")

    /**
     * @suppress
     */
    override fun getState() = ITask.State.Cancelled

    /**
     * @suppress
     */
    override fun transition(newState: ITask.State) = reject()

    /**
     * @suppress
     */
    override fun register(parent: Scheduler) {
        val prev = scheduler
        if (prev is TaskBin) prev.retcon(parent)
        else for (task in inside) parent.add(task) // this is probably as good as we're going to get
        scheduler = parent
        /* do not register this task with the scheduler explicitly */
    }

    /**
     * @suppress
     */
    override fun getId(): Int = 0

    /**
     * @suppress
     */
    override fun getPriority(): Int = 0

    /**
     * @suppress
     */
    override fun getTags(): Set<String> = setOf(BuiltInTags.VIRTUAL, BuiltInTags.DAEMON)

    /**
     * @suppress
     */
    override fun canStart(): Boolean = false

    /**
     * @suppress
     */
    override fun onStart() = reject()

    /**
     * @suppress
     */
    override fun onTick() = reject()

    /**
     * @suppress
     */
    override fun onFinish(completedNormally: Boolean) = reject()

    /**
     * @suppress
     */
    override fun getDependents(): Set<ITask<*>> = emptySet()

    /**
     * @suppress
     */
    override fun stop(cancel: Boolean) = reject()

    override fun <T : ITask<*>> then(other: T): T {
        if (scheduler is TaskBin) throw IllegalStateException("No suitable scheduler assigned when calling 'then'")
        scheduler.add(other)
        for (item in inside) item.then(other)
        return other
    }

    override fun require(before: ITask<*>): VirtualGroup {
        for (item in inside) item.require(before)
        return this
    }

    override fun require(lock: Lock): VirtualGroup {
        for (item in inside) item.require(lock)
        return this
    }

    override fun isVirtual() = true

    companion object {
    }
}
