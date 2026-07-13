package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.prefabs.OneShot
import testTasks.TestLocks
import testTasks.TestTask
import testTasks.TestTask2
import testTasks.TestTask3
import kotlin.test.Test

class testCoroutines {

    private lateinit var test: TestTask
    private lateinit var test2: TestTask2
    val sch = Scheduler()

    @Test
    fun main() {
        var wasB = false
        println("thread on ${Thread.currentThread().name}")
        test = TestTask()
        test2 = TestTask2()
        println("task1 $test")
        println("task2 $test2")

     val testGroup = OneShot {
         sch.add(test)
             .then(test2)
     }

        sch.add(TestTask3().require(TestLocks.DRIVE_MOTORS)).then(OneShot{println("hi")})
        sch.add(TestTask().require(TestLocks.DRIVE_MOTORS))


        while(true){
            println(sch.getLockOwner(TestLocks.DRIVE_MOTORS))
            sch.tick()
            Thread.sleep(10)
        }

    }

}