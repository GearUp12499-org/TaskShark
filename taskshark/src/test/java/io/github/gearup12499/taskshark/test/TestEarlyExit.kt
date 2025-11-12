package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

abstract class TestEarlyExit<T: Scheduler> : SchedulerImplTest<T>() {
    class WithFastScheduler: TestEarlyExit<FastScheduler>(), FastSchedulerImplMixin

    @Test
    fun `test early finish in onStart`() {
        testing(sch) {
            sch.add(object : Task.Anonymous() {
                override fun onStart() {
                    finish()
                    fail { "shouldn't get here!!" }
                }

                override fun onTick(): Boolean {
                    fail { "shouldn't get here!!" }
                }
            })
            runToCompletion(sch)
        }
    }

    @Test
    fun `test early cancel in onStart`() {
        testing(sch) {
            sch.add(object : Task.Anonymous() {
                override fun onStart() {
                    stop()
                    fail { "shouldn't get here!!" }
                }

                override fun onTick(): Boolean {
                    fail { "shouldn't get here!!" }
                }
            })
            runToCompletion(sch)
        }
    }

    @Test
    fun `test early finish in onTick is not reentrant`() {
        testing(sch) {
            sch.add(object : Task.Anonymous() {
                var n = 0

                override fun onTick(): Boolean {
                    if (n++ == 0) finish()
                    fail { "shouldn't get here!!" }
                }
            })
            runToCompletion(sch)
        }
    }

    @Test
    fun `test early cancel in onTick is not reentrant`() {
        testing(sch) {
            sch.add(object : Task.Anonymous() {
                var n = 0

                override fun onTick(): Boolean {
                    if (n++ == 0) stop()
                    fail { "shouldn't get here!!" }
                }
            })
            runToCompletion(sch)
        }
    }
}