package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.Task

open class Group(vararg tasks: Task): Task() {
    val tasks = tasks.toList()
    var firstTime = true
    var n = 0


    override fun onStart() {
    }

    override fun onTick(): Boolean {
        if(firstTime){
            tasks[n].onStart()
            firstTime = false
        }

        if(tasks[n].onTick()){
            tasks[n].onFinish(true)
            n++
            firstTime = true
        }

        return (n == tasks.size)

    }

    override fun onFinish(completedNormally: Boolean) {

    }
}