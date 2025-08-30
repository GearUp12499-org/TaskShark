package io.github.gearup12499.taskshark.kt

import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.api.SupportsAdd
import io.github.gearup12499.taskshark.kt.ParallelBuilder.ActualGroupImpl
import io.github.gearup12499.taskshark.kt.ParallelBuilder.VirtualGroupImpl
import io.github.gearup12499.taskshark.prefabs.Group
import io.github.gearup12499.taskshark.prefabs.OneShot
import io.github.gearup12499.taskshark.prefabs.VirtualGroup
import io.github.gearup12499.taskshark.prefabs.Wait

@DslMarker
internal annotation class TaskDsl

@TaskDsl
sealed class TaskBuilder : SupportsAdd {
    companion object {
        val VIRTUAL_GROUPS = ::VirtualGroupImpl
        val ACTUAL_GROUPS = ::ActualGroupImpl
    }

    abstract override fun <T: ITask<*>> add(task: T): T

    var groupBuilder: () -> ParallelBuilder<*> = VIRTUAL_GROUPS

    fun allOf(groupConfig: ParallelBuilder<*>.() -> Unit) = add(groupBuilder().also(groupConfig).build())

    open fun run(block: () -> Unit) = add(OneShot(block))
    open fun wait(duration: Seconds) = add(Wait.s(duration.value))
    open fun wait(duration: Milliseconds) = add(Wait.ms(duration.value))

    @JvmInline
    value class Seconds(val value: Double)
    @JvmInline
    value class Milliseconds(val value: Double)

    val Number.s: Seconds get() = Seconds(toDouble())
    val Number.ms: Milliseconds get() = Milliseconds(toDouble())
}

@TaskDsl
abstract class ParallelBuilder<Result: ITask<Result>> : TaskBuilder() {
    protected val tasks: MutableList<ITask<*>> = mutableListOf()

    override fun <T : ITask<*>> add(task: T): T {
        tasks.add(task)
        return task
    }

    abstract fun build(): Result

    inline fun sequence(seqConfig: SequenceBuilder.() -> Unit) {
        SequenceBuilder(this).seqConfig()
    }

    class VirtualGroupImpl : ParallelBuilder<VirtualGroup>() {
        override fun build() = VirtualGroup {
            tasks.forEach(::add)
        }
    }

    class ActualGroupImpl : ParallelBuilder<Group>() {
        override fun build() = Group {
            tasks.forEach(::add)
        }
    }
}

@TaskDsl
class SequenceBuilder(val target: SupportsAdd) : TaskBuilder() {
    private var chainElement: ITask<*>? = null

    override fun <T : ITask<*>> add(task: T): T {
        chainElement = (chainElement?.then(task) ?: target.add(task))
        return task
    }
}

inline fun Scheduler.build(block: SequenceBuilder.() -> Unit) = SequenceBuilder(this).block()
