package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.prefabs.VirtualGroup
import kotlin.test.Test

object TestVirtualGroup {
    @Test
    fun `test VirtualGroup as first task`() {
        val sch = FastScheduler()
        val tt = TestTasks()
        sch.add(VirtualGroup {})
            .then(tt.RequireExecution())
        sch.tick()
        runToCompletion(sch)
        tt.assertPassed()
    }

    @Test
    fun `test VirtualGroup as middle task`() {
        val sch = FastScheduler()
        val tt = TestTasks()
        sch.add(tt.RequireExecution())
            .then(VirtualGroup {})
            .then(tt.RequireExecution())
        sch.tick()
        runToCompletion(sch)
        tt.assertPassed()
    }
}