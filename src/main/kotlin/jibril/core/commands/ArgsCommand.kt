package jibril.core.commands

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.intellij.lang.annotations.MagicConstant
import xyz.cuteclouds.utils.args.ArgParser
import xyz.cuteclouds.utils.args.ParserOptions
import xyz.cuteclouds.utils.args.tuples.Tuple

abstract class ArgsCommand(
    @MagicConstant(flagsFromClass = ParserOptions::class)
    private val options: Int = ParserOptions.DEFAULT
) : CommandWithArgs<Tuple>() {
    override fun args(event: GuildMessageReceivedEvent, args: String): Tuple = ArgParser(event.message.contentRaw, options).parse()
}