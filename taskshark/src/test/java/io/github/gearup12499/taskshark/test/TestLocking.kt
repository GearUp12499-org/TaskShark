package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Lock
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task
import kotlin.test.Test

abstract class TestLocking<T: Scheduler> : SchedulerImplTest<T>() {
    class WithFastScheduler: TestLocking<FastScheduler>(), FastSchedulerImplMixin

    val lock = Lock.StrLock("torch")

    internal class MarkExecTime(val to: MutableMap<ITask, Int>): Task(), Testable {
        private var startedAt: Int = -1

        override fun onStart() {
            startedAt = scheduler!!.getTickCount()
            to.put(this, startedAt)
            passed = true
        }

        override fun onTick(): Boolean {
            return scheduler!!.getTickCount() - startedAt >= 1
        }

        override var passed = false
    }

    @Test
    fun `test acquire and release simul`() {
        val lock1 = lock.derive()
        val storage = mutableMapOf<ITask, Int>()
        testing(sch) {
            val t1 = MarkExecTime(storage)
            active.add(t1)
            t1.require(lock1)
            val t2 = MarkExecTime(storage)
            active.add(t2)
            t2.require(lock1)

            sch.addAll(t1, t2)
            runToCompletion(sch)

            assert(storage.values.toSet().size == storage.size) {
                "Tasks started in the same tick"
            }
        }
    }

    @Test
    fun `test lock anti-steal`() {
        val lock1 = lock.derive()
        val storage = mutableMapOf<ITask, Int>()
        testing(sch) {
            val t1 = MarkExecTime(storage)
            val t2 = MarkExecTime(storage)
            val t3 = MarkExecTime(storage)
            active.add(t1)
            active.add(t2)
            active.add(t3)
            t1.require(lock1)
            t2.require(lock1)
            t3.require(lock1)
            t1.then(t3)
            sch.addAll(t1, t2, t3)

            runToCompletion(sch)
            assert(storage[t3]!! < storage[t2]!!) { "Lock was 'stolen' by t2" }
        }
    }

    @Test
    fun `test lock blocking`() {
        val lock1 = lock.derive()
        val lock2 = lock.derive()
        testing(sch) {

        }
    }
}