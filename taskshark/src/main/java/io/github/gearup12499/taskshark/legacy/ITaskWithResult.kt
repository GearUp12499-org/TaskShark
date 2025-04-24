package io.github.gearup12499.taskshark.legacy

enum class ResolveReject {
    RESOLVE,
    REJECT
}

interface ITaskWithResult<T> : ITaskOld {
    fun getResultMaybe(): T?
    fun getResult(): T = getResultMaybe() ?: throw IllegalStateException("No result was available")
    fun hasResult(): Boolean

    fun <X : ITaskConsumer<T>> then(task: X): X {
        then(task as ITaskOld)
        task.upstreamTyped(this)
        return task
    }
}

interface ITaskConsumer<T> : ITaskOld {
    fun upstreamTyped(provider: ITaskWithResult<T>)
}


abstract class TaskWithResultTemplate<T>(scheduler: SchedulerOld) : TaskTemplate(scheduler), ITaskWithResult<T> {
    private var result: T? = null

    /**
     * Sets the result of this task. Only usable once per task execution.
     * Usually a good idea to put this in onFinished.
     */
    protected fun setResult(value: T) {
        if (result == null) {
            result = value
        } else throw IllegalStateException("A result was already assigned to this task")
    }

    protected fun setResultMaybe(value: T) {
        if (result == null) result = value
    }

    override fun hasResult(): Boolean = result != null

    override fun getResultMaybe(): T? = result

    override fun then(configure: Task.() -> Unit): Task {
        return super<TaskTemplate>.then(configure)
    }

    override fun then(polyChain: Pair<ITaskOld, ITaskOld>): ITaskOld {
        return super<TaskTemplate>.then(polyChain)
    }
}