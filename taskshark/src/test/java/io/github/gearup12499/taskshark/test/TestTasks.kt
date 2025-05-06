package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task
import io.github.gearup12499.taskshark.api.LogOutlet

fun runToCompletion(sch: Scheduler) {
    while (sch.tasks.any { (_, task) ->
            if (task.getTags().contains("daemon")) true
            else when(task.getState()) {
                ITask.State.Finished, ITask.State.Cancelled -> false
                else -> true
            }
        }) {
        sch.tick()
        if (sch.getTickCount() > 10_000) // arbitrary number
            throw AssertionError("Execution timed out")
    }
}
fun generateRunSummary(sch: Scheduler) = buildString {
    append("ran on scheduler $sch (${sch.id}):\n")
    append(" total ${sch.getTickCount()} ticks\n")
    append(" total ${sch.tasks.size} tasks\n")
    fun byState(state: ITask.State): Int = sch.tasks.values.filter { it.getState() == state }.size
    for (state in ITask.State.entries) {
        append("  ${state.name}: ${byState(state)}\n")
    }
}

inline fun testing(sch: Scheduler, block: TestTasks.() -> Unit) {
    LogOutlet.currentLogger.level = LogOutlet.Level.Debug
    val tt = TestTasks()
    try {
        tt.block()
        tt.assertPassed()
    } finally {
        println(generateRunSummary(sch))
    }
}

class TestTasks {
    val active: MutableSet<ITask> = mutableSetOf()

    fun assertPassed() {
        val failures: MutableList<ITask> = mutableListOf()
        for (task in active) {
            if (task !is Testable) continue
            if (!task.passed) {
                failures.add(task)
            }
        }
        if (!failures.isEmpty()) throw AssertionError(buildString {
            append(failures.size).append(" tasks failed:\n")
            for (failure in failures) {
                append("  ")
                append(failure.describeVerbose())
                append("\n")
            }
        })
    }

    inner class RequireExecution() : Task(), Testable {
        init { active.add(this) }

        override fun onStart() {
            println("task $id executed")
            passed = true
        }

        override fun onTick() = true

        override var passed = false
    }
}