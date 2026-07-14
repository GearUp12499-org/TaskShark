package testTasks

import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task

object Combo {

    fun combo1(sch: Scheduler): Task{

       return sch.add(TestTask())
                .then(TestTask2())


    }

}