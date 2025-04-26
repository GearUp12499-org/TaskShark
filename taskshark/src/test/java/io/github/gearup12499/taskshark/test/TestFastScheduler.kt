package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
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
    }
}
