package pw.aru.core.listeners

import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.hooks.EventListener

abstract class OptimizedListener<in T : Event>(private val c: Class<T>) : EventListener {

    abstract fun event(event: T)

    override fun onEvent(event: Event) {
        if (c.isInstance(event)) event(c.cast(event))
    }
}

inline fun <reified T : Event> listener(noinline onEvent: EventListener.(T) -> Unit) = object : EventListener {
    override fun onEvent(it: Event) {
        if (it is T) onEvent(this, it)
    }
}