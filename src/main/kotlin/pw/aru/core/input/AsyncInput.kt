package pw.aru.core.input

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.extensions.classOf
import java.util.concurrent.TimeUnit

abstract class AsyncInput protected constructor(private val eventWaiter: EventWaiter, private val timeout: Long, private val unit: TimeUnit) {

    init {
        waitForNextEvent()
    }

    protected abstract fun call(event: GuildMessageReceivedEvent)

    protected abstract fun filter(event: GuildMessageReceivedEvent): Boolean

    protected abstract fun timeout()

    protected fun waitForNextEvent() {
        eventWaiter.waitForEvent(classOf<GuildMessageReceivedEvent>(), ::filter, ::call, timeout, unit, ::timeout)
    }
}

abstract class AsyncCommandsInput protected constructor(eventWaiter: EventWaiter, timeout: Long, unit: TimeUnit) : AsyncInput(eventWaiter, timeout, unit) {
    override fun call(event: GuildMessageReceivedEvent) {
        val parts = event.message.contentRaw.split(' ', limit = 2)
        CommandContext(event, parts.getOrNull(1) ?: "").onCommand(parts[0])
    }

    protected abstract fun CommandContext.onCommand(command: String)
}

abstract class AsyncCommandInput protected constructor(eventWaiter: EventWaiter, timeout: Long, unit: TimeUnit, private val command: String) : AsyncInput(eventWaiter, timeout, unit) {
    override fun filter(event: GuildMessageReceivedEvent): Boolean = event.message.contentRaw.startsWith(command)

    override fun call(event: GuildMessageReceivedEvent) {
        val parts = event.message.contentRaw.split(' ', limit = 2)
        CommandContext(event, parts.getOrNull(1) ?: "").onCommand()
    }

    protected abstract fun CommandContext.onCommand()
}
