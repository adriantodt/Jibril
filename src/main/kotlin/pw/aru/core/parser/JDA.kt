package pw.aru.core.parser

import com.jagrosh.jdautilities.commons.utils.FinderUtil.*
import net.dv8tion.jda.core.entities.*

fun Args.tryTakeMember(guild: Guild): Member? = mapNextString { findMembers(it, guild).singleOrNull().let { it to (it != null) } }

fun Args.takeMember(guild: Guild): Member = tryTakeMember(guild) ?: throw IllegalStateException("argument is not a valid Member")

fun Args.takeMembers(guild: Guild) = descontructed { takeMember(guild) }

fun Args.takeAllMembers(guild: Guild): List<Member> = generateSequence { tryTakeMember(guild) }.toList()

fun Args.tryTakeRole(guild: Guild): Role? = mapNextString { findRoles(it, guild).singleOrNull().let { it to (it != null) } }

fun Args.takeRole(guild: Guild): Role = tryTakeRole(guild) ?: throw IllegalStateException("argument is not a valid Role")

fun Args.takeRoles(guild: Guild) = descontructed { takeRole(guild) }

fun Args.takeAllRoles(guild: Guild): List<Role> = generateSequence { tryTakeRole(guild) }.toList()

fun Args.tryTakeEmote(guild: Guild): Emote? = mapNextString { findEmotes(it, guild).singleOrNull().let { it to (it != null) } }

fun Args.takeEmote(guild: Guild): Emote = tryTakeEmote(guild) ?: throw IllegalStateException("argument is not a valid Emote")

fun Args.takeEmotes(guild: Guild) = descontructed { takeEmote(guild) }

fun Args.takeAllEmotes(guild: Guild): List<Emote> = generateSequence { tryTakeEmote(guild) }.toList()

fun Args.tryTakeTextChannel(guild: Guild): TextChannel? = mapNextString { findTextChannels(it, guild).singleOrNull().let { it to (it != null) } }

fun Args.takeTextChannel(guild: Guild): TextChannel = tryTakeTextChannel(guild) ?: throw IllegalStateException("argument is not a valid TextChannel")

fun Args.takeTextChannels(guild: Guild) = descontructed { takeTextChannel(guild) }

fun Args.takeAllTextChannels(guild: Guild): List<TextChannel> = generateSequence { tryTakeTextChannel(guild) }.toList()

fun Args.tryTakeVoiceChannel(guild: Guild): VoiceChannel? = mapNextString { findVoiceChannels(it, guild).singleOrNull().let { it to (it != null) } }

fun Args.takeVoiceChannel(guild: Guild): VoiceChannel = tryTakeVoiceChannel(guild) ?: throw IllegalStateException("argument is not a valid VoiceChannel")

fun Args.takeVoiceChannels(guild: Guild) = descontructed { takeVoiceChannel(guild) }

fun Args.takeAllVoiceChannels(guild: Guild): List<VoiceChannel> = generateSequence { tryTakeVoiceChannel(guild) }.toList()

fun Args.tryTakeCategory(guild: Guild): Category? = mapNextString { findCategories(it, guild).singleOrNull().let { it to (it != null) } }

fun Args.takeCategory(guild: Guild): Category = tryTakeCategory(guild) ?: throw IllegalStateException("argument is not a valid Category")

fun Args.takeCategories(guild: Guild) = descontructed { takeCategory(guild) }

fun Args.takeAllCategories(guild: Guild): List<Category> = generateSequence { tryTakeCategory(guild) }.toList()
