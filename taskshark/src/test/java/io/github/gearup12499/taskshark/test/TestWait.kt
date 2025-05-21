package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.api.LogOutlet
import io.github.gearup12499.taskshark.prefabs.Wait
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.markNow

abstract class TestWait<T: Scheduler> : SchedulerImplTest<T>() {
    class WithFastScheduler: TestWait<FastScheduler>(), FastSchedulerImplMixin

    val duration = 10 // ms
    val durationType = duration.milliseconds

    @Test
    fun `test wait at least ms`() {
        var end: TimeSource.Monotonic.ValueTimeMark? = null
        var start: TimeSource.Monotonic.ValueTimeMark? = null
        testing(sch) {
            sch.add(Wait.ms(duration))
                .then(RequireExecution())

            start = markNow()
            runToCompletion(sch)
            end = markNow()
        }
        assert((end!! - start!! - durationType).isPositive()) {
            "Too fast: actually took ${end - start} to complete; expected $durationType"
        }
    }

    val minSpinPerSecond = 5000 // Hz; minimum

    @Test
    fun `test spin perf`() {
        var start: TimeSource.Monotonic.ValueTimeMark? = null
        var end: TimeSource.Monotonic.ValueTimeMark? = null
        testing(sch) {
            sch.add(Wait.ms(100))
                .then(RequireExecution())
            start = markNow()
            runToCompletion(sch, 1_000_000_000) // run max 1B steps
            end = markNow()
        }
        val duration = end!! - start!!
        val spinRate = sch.getTickCount() / duration.toDouble(DurationUnit.SECONDS)
        assert(spinRate >= minSpinPerSecond) {
            ("FastScheduler isn't performant enough" +
                    " - expected at least ${minSpinPerSecond}Hz, actually %.2fHz").format(spinRate)
        }
        LogOutlet.currentLogger.info {
            "Benchmark results: %.2fHz (minimum: ${minSpinPerSecond}Hz)".format(spinRate)
        }
    }
}