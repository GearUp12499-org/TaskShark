package testTasks

import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task
import io.github.gearup12499.taskshark.prefabs.Group
import io.github.gearup12499.taskshark.prefabs.OneShot

object Combo {

    val combo1 = Group(
        TestTask(),
        TestTask2(),
        OneShot { println("hi") }
    )

}