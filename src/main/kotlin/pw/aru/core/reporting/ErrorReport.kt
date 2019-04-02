package pw.aru.core.reporting

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.message.Message
import pw.aru.core.commands.ICommand

internal data class ErrorReport internal constructor(
    val command: ICommand?,
    val timestamp: Long?,
    val errorId: String?,
    val throwable: Throwable?,
    val underlyingThrowable: Throwable?,
    val log: String?,
    val guild: Guild?,
    val channel: TextChannel?,
    val message: Message?,
    val member: Member?,
    val extra: Map<String, Any?>?
)
