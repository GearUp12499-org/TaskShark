package testTasks

import io.github.gearup12499.taskshark.Task

open class TestTask: Task(){
    var count = 0

    override fun onStart(){
        println("start")
    }

    override fun onTick(): Boolean {
        println("tick at ${Thread.currentThread().name}")
        count++
        if (count < 20){
            return false
        }else{
            return true
        }
    }

    override fun onFinish(completedNormally: Boolean) {
        println("end")
    }
}