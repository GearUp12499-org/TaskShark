package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task
import org.junit.jupiter.api.Test
import kotlin.test.fail

object ErrorTests {
    inline fun <reified T> expect(block: () -> Unit) {
        try {
            block()
            fail("Expected an exception of type ${T::class}, but no exception occurred")
        } catch (err: Exception) {
            if (err !is T) fail("Expected an exception of type ${T::class}, but instead $err was thrown (of type: ${err::class})")
            println("(ok) threw\n$err")
            return
        }
    }
}

abstract class TestInvalidConstructions<T : Scheduler> : SchedulerImplTest<T>() {
    class WithFastScheduler : TestInvalidConstructions<FastScheduler>(), FastSchedulerImplMixin

    @Test
    fun `chaining tasks without schedules`() {
        ErrorTests.expect<IllegalStateException> {
            val dangling = object: Task.Anonymous() {
                override fun onTick(): Boolean = true
            }
            val dangling2 = object: Task.Anonymous() {
                override fun onTick(): Boolean = true
            }

            dangling.then(dangling2)
        }
    }
}