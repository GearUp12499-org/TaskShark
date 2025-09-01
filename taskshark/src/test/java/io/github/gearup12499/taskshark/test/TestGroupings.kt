package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.prefabs.Group
import io.github.gearup12499.taskshark.prefabs.OneShot
import io.github.gearup12499.taskshark.prefabs.VirtualGroup
import io.github.gearup12499.taskshark.prefabs.WaitTicks
import kotlin.test.Test

abstract class TestGroupings<T: Scheduler> : SchedulerImplTest<T>() {
    class WithFastScheduler: TestGroupings<FastScheduler>(), FastSchedulerImplMixin

    @Test
    fun `test VirtualGroup as first task`() {
        val tt = TestTasks()
        sch.add(VirtualGroup {})
            .then(tt.RequireExecution())
        sch.tick()
        runToCompletion(sch)
        tt.assertPassed()
    }

    @Test
    fun `test VirtualGroup as middle task`() {
        val tt = TestTasks()
        sch.add(tt.RequireExecution())
            .then(VirtualGroup {})
            .then(tt.RequireExecution())
        sch.tick()
        runToCompletion(sch)
        tt.assertPassed()
    }

    @Test
    fun `test VirtualGroup with chaining inside`() {
        val tt = TestTasks()
        sch.add(VirtualGroup {
            add(tt.RequireExecution())
                .then(tt.RequireExecution())
            add(tt.RequireExecution().then(tt.RequireExecution()))
        })
        runToCompletion(sch)
        tt.assertPassed()
    }

    @Test
    fun `test multi-depend with VG`() {
        testing(sch) {
            lateinit var first: ITask<*>; lateinit var second: ITask<*>
            sch.add(VirtualGroup {
                first = add(WaitTicks(5))
                second = add(WaitTicks(10))
            }).then(OneShot {
                assert(first.getState() == ITask.State.Finished)
                assert(second.getState() == ITask.State.Finished)
            }).then(RequireExecution())

            runToCompletion(sch)
        }
    }

    @Test
    fun `test empty actual group as start`() {
        testing(sch) {
            sch.add(Group {})
                .then(RequireExecution())
            runToCompletion(sch)
            assertPassed()
        }
    }

    @Test
    fun `test empty actual group as middle`() {
        testing(sch) {
            sch.add(RequireExecution())
                .then(Group {})
                .then(RequireExecution())
            runToCompletion(sch)
            assertPassed()
        }
    }
}