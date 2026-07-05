package io.github.gearup12499.taskshark

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * coroutine dispatcher used to force all coroutines to run on the main thread
 * because ftc robots can't multithread
 */
open class Dispatcher: CoroutineDispatcher() {
    protected val activeTasks = ArrayDeque<Runnable>()

    fun runTasks(){
        val size = activeTasks.size
        for(i in 1..size){
            val task = activeTasks.removeFirstOrNull()
            task?.run()
        }
    }

    override fun dispatch(
        context: CoroutineContext,
        block: kotlinx.coroutines.Runnable
    ) {
       activeTasks.add(block)
    }


}