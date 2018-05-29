package jibril.core.listeners

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import jibril.core.listeners.EventListeners.submit
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.hooks.EventListener

class AsyncEventWaiter(private val eventWaiter: EventWaiter) : EventListener {
    override fun onEvent(event: Event) {
        submit("Event:${event.javaClass.simpleName}") {
            eventWaiter.onEvent(event)
        }
    }
}