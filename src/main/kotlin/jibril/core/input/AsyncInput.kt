package jibril.core.input

import jibril.Jibril
import jibril.utils.extensions.classOf
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.TimeUnit

abstract class AsyncInput protected constructor(private val timeout: Long, private val unit: TimeUnit) {

    protected abstract fun call(event: GuildMessageReceivedEvent)

    protected abstract fun filter(event: GuildMessageReceivedEvent): Boolean

    protected abstract fun timeout()

    protected fun waitForNextEvent() {
        Jibril.eventWaiter
            .waitForEvent(classOf<GuildMessageReceivedEvent>(), ::filter, ::call, timeout, unit, ::timeout)
    }
}

abstract class AsyncCommandInput protected constructor(timeout: Long, unit: TimeUnit) : AsyncInput(timeout, unit) {
    override fun call(event: GuildMessageReceivedEvent) {
        val parts = event.message.contentRaw.split(' ')
        onCommand(event, parts[0], parts.getOrNull(1) ?: "")
    }

    protected abstract fun onCommand(event: GuildMessageReceivedEvent, command: String, args: String)
}
