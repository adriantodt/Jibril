package pw.aru.bot.input

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.shard.DiscordEvent
import pw.aru.bot.commands.context.CommandContext
import java.util.concurrent.TimeUnit

abstract class AsyncInput protected constructor(
    private val catnip: Catnip,
    private val timeout: Long,
    private val unit: TimeUnit
) {

    init {
        waitForNextEvent()
    }

    protected abstract fun call(message: Message)

    protected abstract fun filter(message: Message): Boolean

    protected abstract fun timeout()

    protected fun waitForNextEvent() {
        catnip.observe(DiscordEvent.MESSAGE_CREATE)
            .filter(::filter)
            .singleElement()
            .timeout(timeout, unit) {
                timeout()
            }
            .subscribe(::call)
    }
}

abstract class AsyncCommandsInput protected constructor(catnip: Catnip, timeout: Long, unit: TimeUnit) :
    AsyncInput(catnip, timeout, unit) {
    override fun call(message: Message) {
        val parts = message.content().split(' ', limit = 2)
        CommandContext(message, parts.getOrNull(1) ?: "", emptySet()).onCommand(parts[0])
    }

    protected abstract fun CommandContext.onCommand(command: String)
}

abstract class AsyncCommandInput protected constructor(
    catnip: Catnip,
    timeout: Long,
    unit: TimeUnit,
    private val command: String
) : AsyncInput(catnip, timeout, unit) {
    override fun filter(message: Message): Boolean = message.content().startsWith(command)

    override fun call(message: Message) {
        val parts = message.content().split(' ', limit = 2)
        CommandContext(message, parts.getOrNull(1) ?: "", emptySet()).onCommand()
    }

    protected abstract fun CommandContext.onCommand()
}
