package pw.aru.core.listeners

import java.lang.Thread.currentThread
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

object EventListeners {
    private val sharedPool = Executors.newCachedThreadPool()!!

    fun <T> submit(name: String, task: () -> T): Future<T> {
        return sharedPool.submit(Callable<T> {
            val t = currentThread()
            val n = t.name
            t.name = name

            try {
                task()
            } finally {
                t.name = n
            }
        })
    }
}