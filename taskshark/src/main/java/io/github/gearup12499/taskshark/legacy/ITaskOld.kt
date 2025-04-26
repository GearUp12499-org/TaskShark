package io.github.gearup12499.taskshark.legacy

import io.github.gearup12499.taskshark.legacy.ITaskOld.State

@Deprecated("the legacy api is deprecated")
interface ITaskOld {
    enum class State(val order: Int) {
        NotStarted(0),
        Starting(1),
        Ticking(2),
        Finishing(3),
        Finished(4),
        Cancelled(4),
    }

    var scheduler: SchedulerOld
    val state: State
    val myId: Int?
    var name: String
    val daemon: Boolean
    val isStartRequested: Boolean
    fun onRequest() = isStartRequested

    fun isBypass() = false

    // Lifecycle
    fun transition(newState: State)
    fun register()

    fun invokeCanStart(): Boolean
    fun invokeOnStart()
    fun invokeOnTick()
    fun invokeIsCompleted(): Boolean
    fun invokeOnFinish()

    fun requirements(): Set<SharedResource>
    infix fun waitsFor(after: ITaskOld)

    fun requestStart()
    fun requestStop(cancel: Boolean) {
        scheduler.filteredStop({ it === this }, cancel)
    }

    fun requestStop() = requestStop(true)
    fun finishEarly() = requestStop(false)

    fun then(configure: Task.() -> Unit): Task {
        val task = Task(scheduler)
        task.configure()
        task waitsFor this
        task.register() // ready to go
        task.name = getCaller()
        return task
    }

    fun <T : ITaskOld> then(task: T): T {
        task.scheduler = scheduler
        task waitsFor this
        task.register()
        task.name = getCaller()
        return task
    }

    fun then(polyChain: Pair<ITaskOld, ITaskOld>): ITaskOld {
        this.then(polyChain.first)
        return polyChain.second
    }

    fun display(indent: Int, write: (String) -> Unit) {}
}

@Deprecated("the legacy api is deprecated")
abstract class TaskWithChaining() : ITaskOld {

    private var waitFor: MutableSet<ITaskOld> = mutableSetOf()

    fun getWaitsFor() = waitFor as Set<ITaskOld>

    override fun waitsFor(after: ITaskOld) {
        waitFor.add(after)
    }

    override fun invokeCanStart(): Boolean {
        if (waitFor.any { it.state == State.Cancelled }) requestStop(true)
        if (waitFor.any { it.state != State.Finished }) return false
        return true
    }
}

@Deprecated("the legacy api is deprecated")
abstract class TaskTemplate(override var scheduler: SchedulerOld) : TaskWithChaining(), ITaskOld {
    final override var state = State.NotStarted
    final override var myId: Int? = null
    private var name2 = "unnamed task"
    override var name: String
        get() = name2
        set(value) {
            name2 = value
        }

    var startedAt = 0
        private set
    override var isStartRequested = false

    override fun transition(newState: State) {
//        println("$this: transition: ${state.name} -> ${newState.name}")
        if (state.order > newState.order) {
            throw IllegalStateException("cannot move from ${state.name} to ${newState.name}")
        }
        if (state == newState) return
        when (newState) {
            State.Starting -> startedAt = scheduler.getTicks()
            State.Finishing -> println("$this: finishing at ${scheduler.getTicks()} (run for ${scheduler.getTicks() - startedAt} ticks)")
            else -> {}
        }
        state = newState
    }

    override fun register() {
        myId = scheduler.register(this)
    }

    override fun requestStart() {
        isStartRequested = true
    }

    override fun requirements(): Set<SharedResource> = setOf()

    override fun invokeOnStart() {}

    override fun invokeOnTick() {}

    override fun invokeIsCompleted() = false

    override fun invokeOnFinish() {}

    override fun invokeCanStart() = super.invokeCanStart()

    override val daemon: Boolean = false

    override fun toString(): String {
        return "task $myId '$name'"
    }
}

@Deprecated("the legacy api is deprecated")
abstract class ConsumingTaskTemplate<T>(scheduler: SchedulerOld) : TaskTemplate(scheduler),
    ITaskConsumer<T> {
    private val typedUpstreams: MutableList<ITaskWithResult<T>> = mutableListOf()

    override fun upstreamTyped(provider: ITaskWithResult<T>) {
        typedUpstreams.add(provider)
    }

    protected val weakResult: T?
        get() = nullableFindReadyUpstream(typedUpstreams)
    protected val result: T
        get() = findReadyUpstream(typedUpstreams)
}

private fun <T> nullableFindReadyUpstream(providers: List<ITaskWithResult<T>>): T? {
    var result: T? = null
    for (item in providers) {
        item.getResultMaybe().let {
            if (result != null) throw IllegalStateException("More than one upstream result is available")
            result = it
        }
    }
    return result
}

private fun <T> findReadyUpstream(providers: List<ITaskWithResult<T>>): T =
    nullableFindReadyUpstream(providers)
        ?: throw IllegalStateException("No upstream results available yet")
