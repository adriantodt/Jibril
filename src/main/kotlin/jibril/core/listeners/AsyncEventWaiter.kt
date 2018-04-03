package jibril.core.listeners

import jibril.Jibril.eventWaiter
import jibril.core.listeners.EventListeners.submit
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.hooks.EventListener

object AsyncEventWaiter : EventListener {
    override fun onEvent(event: Event) {
        submit("Event:${event.javaClass.simpleName}") {
            eventWaiter.onEvent(event)
        }
    }
}