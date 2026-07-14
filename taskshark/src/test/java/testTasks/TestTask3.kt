package testTasks

import io.github.gearup12499.taskshark.Task

class TestTask3: Task(){
    var count = 0

    override fun onStart(){
        println("start3")
    }

    override fun onTick(): Boolean {
        println("tick3 at ${Thread.currentThread().name}")
        count++
        if (count < 20){
            return false
        }else{
            return true
        }
    }

    override fun onFinish(completedNormally: Boolean) {
        println("end3")
    }

    override fun toString(): String {
        return "TestTask3"
    }
}