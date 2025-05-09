package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Lock
import io.github.gearup12499.taskshark.Task
import kotlin.test.Test

object TestFastScheduler {
    val torch = Lock.StrLock("torch")

    @Test fun `construct FastScheduler`() {
        val fs = FastScheduler()
        println("$fs: id ${fs.id}, hash ${fs.hashCode()} = 0x${"%X".format(fs.hashCode())}")
    }

    @Test fun `test single-task cascade behavior`() {
        val fs = FastScheduler()
        val lock = torch.derive()

        var onStartCalled = false
        var onTickCalled = false
        var onFinishCalled = false

        val task = object: Task() {
            override fun getTags(): Set<String> = emptySet()

            init {
                require(lock)
            }

            override fun onStart() {
                onStartCalled = true
            }

            override fun onTick(): Boolean {
                onTickCalled = true
                return true
            }

            override fun onFinish(completedNormally: Boolean) {
                onFinishCalled = true
            }
        }
        task.register(fs)

        fs.tick()
        assert(onStartCalled) { "onStart was never called" }
        assert(onTickCalled) { "onTick was never called" }
        assert(onFinishCalled) { "onFinish was never called" }
        assert(fs.getCurrentEvaluation() == null) { "a task is evaluating after the tick ended" }
        assert(fs.getLockOwner(lock) == null) { "lock was never released" }
    }

    class EarlyExitAndCleanup() : Task(), Testable {
        override val passed: Boolean
            get() = passed1 && passed2
        var passed1 = false
        var passed2 = false

        override fun onStart() {
            passed1 = true
            finish()
        }

        override fun onTick(): Boolean {
            throw AssertionError("Should have never called onTick due to finish in onStart")
        }

        override fun onFinish(completedNormally: Boolean) {
            assert(completedNormally) { "Task did not 'complete normally' despite using finish()" }
            passed2 = true
        }
    }

    @Test fun `test onStart early exit and onFinish cleanup`() {
        val fs = FastScheduler()
        val lock = torch.derive()
        val task = EarlyExitAndCleanup()
        task.require(lock)
        task.register(fs)
        fs.tick()
        assert(task.passed) { "Task did not complete successfully" }
        assert(task.getState() == ITask.State.Finished) { "Task ended in incorrect state: ${task.getState()}" }
        assert(fs.getCurrentEvaluation() == null) { "a task is evaluating after the tick ended" }
        assert(fs.getLockOwner(lock) == null) { "lock was never released" }
    }

    @Test fun `test then deferred`() {
        val fs = FastScheduler()
        testing(fs) {
            fs.add(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
            runToCompletion(fs)
        }
    }

    @Test fun `test then immediate`() {
        val fs = FastScheduler()
        testing(fs) {
            fs.add(RequireExecution())
                .then(RequireExecution())
                .then(RequireExecution())
                .then(RequireExecution())
                .then(RequireExecution())
            runToCompletion(fs)
        }
    }
}
