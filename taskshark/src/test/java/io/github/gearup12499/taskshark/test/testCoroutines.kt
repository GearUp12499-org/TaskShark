package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.prefabs.Group
import io.github.gearup12499.taskshark.prefabs.OneShot
import testTasks.Combo
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
        println("thread on ${Thread.currentThread().name}")
        test = TestTask()
        test2 = TestTask2()
        println("task1 $test")
        println("task2 $test2")

        val group = OneShot{
            sch.add(test).then(TestTask3())
        }

        sch.add(Group(
            test,
            test2
        )).require(TestLocks.DRIVE_MOTORS)
        sch.add(TestTask3()).require(TestLocks.DRIVE_MOTORS)

        println(test.dependedLocks())

        while(true){
            sch.tick()
            Thread.sleep(10)
        }

    }

}