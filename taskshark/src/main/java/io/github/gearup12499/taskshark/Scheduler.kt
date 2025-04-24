package io.github.gearup12499.taskshark

abstract class Scheduler {
    @JvmField val tasks = mutableMapOf<Int, ITask>()
    @JvmField protected val evalStack = ArrayDeque<ITask>()

    abstract fun register(task: ITask): Int

    open fun <T: ITask> add(task: T): T {
        task.register(this)
        return task
    }

    open fun getOpenEvaluations(): ArrayDeque<ITask> = evalStack
    open fun getCurrentEvaluation(): ITask? = evalStack.lastOrNull()

    protected inline fun using(t: ITask, block: () -> Unit) {
        evalStack.addLast(t);
        try {
            block()
        } finally {
            assert(evalStack.removeLast() === t) {
                "Evaluation stack failed: expected to remove $t, but got something else instead (TaskShark internal error)"
            }
        }
    }
}