package testTasks

import io.github.gearup12499.taskshark.Task

class TestTask2: Task(){
    var count = 0

    override fun onStart(){
        println("start2")
    }

    override fun onTick(): Boolean {
        println("tick2 at ${Thread.currentThread().name}")
        count++
        if (count < 20){
            return false
        }else{
            return true
        }
    }

    override fun onFinish(completedNormally: Boolean) {
        println("end2")
    }
}