package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.Scheduler
import kotlin.test.BeforeTest

interface SchedulerInit<T: Scheduler> {
    fun newInstance(): T
}

interface FastSchedulerImplMixin: SchedulerInit<FastScheduler> {
    override fun newInstance() = FastScheduler()
}

abstract class SchedulerImplTest<T: Scheduler>: SchedulerInit<T> {
    protected lateinit var sch: T

    @BeforeTest
    open fun before() {
        sch = newInstance()
    }
}