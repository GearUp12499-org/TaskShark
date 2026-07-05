package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Dispatcher
import kotlinx.coroutines.*
import kotlin.test.Test

class TestThenCoroutines {

    val dispatcher = Dispatcher()
    val scope = CoroutineScope(dispatcher)

    val sch = Scheduler()

    @Test
    fun main() {
        println("thread on ${Thread.currentThread().name}")




        while(true){
            sch.tick()
            Thread.sleep(100)

        }

    }

}