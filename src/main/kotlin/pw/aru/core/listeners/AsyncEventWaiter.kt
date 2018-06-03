package pw.aru.core.listeners

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.hooks.EventListener
import pw.aru.core.listeners.EventListeners.submitTask

class AsyncEventWaiter(private val eventWaiter: EventWaiter) : EventListener {
    override fun onEvent(event: Event) {
        submitTask("Event:${event.javaClass.simpleName}") {
            eventWaiter.onEvent(event)
        }
    }
}