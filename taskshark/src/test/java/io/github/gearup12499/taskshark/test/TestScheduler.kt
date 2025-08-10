package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Lock
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task
import io.github.gearup12499.taskshark.prefabs.OneShot
import io.github.gearup12499.taskshark.prefabs.Wait
import kotlin.test.Test

abstract class TestScheduler<T: Scheduler> : SchedulerImplTest<T>() {
    class WithFastScheduler: TestScheduler<FastScheduler>(), FastSchedulerImplMixin

    val torch = Lock.StrLock("torch")

    @Test fun construct() {
        println("$sch: id ${sch.id}, hash ${sch.hashCode()} = 0x${"%X".format(sch.hashCode())}")
    }

    @Test fun `test single-task cascade behavior`() {
        val lock = torch.derive()

        var onStartCalled = false
        var onTickCalled = false
        var onFinishCalled = false

        val task = object: Task.Anonymous() {
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
        task.register(sch)

        sch.tick()
        assert(onStartCalled) { "onStart was never called" }
        assert(onTickCalled) { "onTick was never called" }
        assert(onFinishCalled) { "onFinish was never called" }
        assert(sch.getCurrentEvaluation() == null) { "a task is evaluating after the tick ended" }
        assert(sch.getLockOwner(lock) == null) { "lock was never released" }
    }

    class EarlyExitAndCleanup() : Task<EarlyExitAndCleanup>(), Testable {
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
        val lock = torch.derive()
        val task = EarlyExitAndCleanup()
        task.require(lock)
        task.register(sch)
        sch.tick()
        assert(task.passed) { "Task did not complete successfully" }
        assert(task.getState() == ITask.State.Finished) { "Task ended in incorrect state: ${task.getState()}" }
        assert(sch.getCurrentEvaluation() == null) { "a task is evaluating after the tick ended" }
        assert(sch.getLockOwner(lock) == null) { "lock was never released" }
    }

    @Test fun `test then deferred`() {
        testing(sch) {
            sch.add(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
                .then(RequireExecutionDeferred())
            runToCompletion(sch)
        }
    }

    @Test fun `test then immediate`() {
        testing(sch) {
            sch.add(RequireExecution())
                .then(RequireExecution())
                .then(RequireExecution())
                .then(RequireExecution())
                .then(RequireExecution())
            runToCompletion(sch)
        }
    }

    @Test fun `test multi-depend manually`() {
        testing(sch) {
            val one = sch.add(Wait.ms(5))
            val two = sch.add(Wait.ms(10))
            val three = OneShot {
                assert(one.getState() == ITask.State.Finished)
                assert(two.getState() == ITask.State.Finished)
            }
            one.then(three)
            two.then(three)
            three.then(RequireExecution())

            runToCompletion(sch)
        }
    }
}
