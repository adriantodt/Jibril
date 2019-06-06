package pw.aru.core.parser

import com.mewna.catnip.entity.channel.Category
import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.channel.VoiceChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.guild.Role
import com.mewna.catnip.entity.misc.Emoji
import pw.aru.libs.catnip.entityfinder.EntityFinder.*

fun Args.tryTakeMember(guild: Guild): Member? = mapNextString { findMembers(it, guild).singleOrNull().toMapResult() }

fun Args.takeMember(guild: Guild): Member = tryTakeMember(guild) ?: throw IllegalStateException("argument is not a valid Member")

fun Args.takeAllMembers(guild: Guild): List<Member> = generateSequence { tryTakeMember(guild) }.toList()

fun Args.tryTakeRole(guild: Guild): Role? = mapNextString { findRoles(it, guild).singleOrNull().toMapResult() }

fun Args.takeRole(guild: Guild): Role = tryTakeRole(guild) ?: throw IllegalStateException("argument is not a valid Role")

fun Args.takeAllRoles(guild: Guild): List<Role> = generateSequence { tryTakeRole(guild) }.toList()

fun Args.tryTakeEmoji(guild: Guild): Emoji? =
    mapNextString { findEmojis(it, guild).singleOrNull().toMapResult() }

fun Args.takeEmoji(guild: Guild): Emoji =
    tryTakeEmoji(guild) ?: throw IllegalStateException("argument is not a valid Emoji")

fun Args.takeAllEmojis(guild: Guild): List<Emoji> = generateSequence { tryTakeEmoji(guild) }.toList()

fun Args.tryTakeTextChannel(guild: Guild): TextChannel? =
    mapNextString { findTextChannels(it, guild).singleOrNull().toMapResult() }

fun Args.takeTextChannel(guild: Guild): TextChannel = tryTakeTextChannel(guild) ?: throw IllegalStateException("argument is not a valid TextChannel")

fun Args.takeAllTextChannels(guild: Guild): List<TextChannel> = generateSequence { tryTakeTextChannel(guild) }.toList()

fun Args.tryTakeVoiceChannel(guild: Guild): VoiceChannel? =
    mapNextString { findVoiceChannels(it, guild).singleOrNull().toMapResult() }

fun Args.takeVoiceChannel(guild: Guild): VoiceChannel = tryTakeVoiceChannel(guild) ?: throw IllegalStateException("argument is not a valid VoiceChannel")

fun Args.takeAllVoiceChannels(guild: Guild): List<VoiceChannel> = generateSequence { tryTakeVoiceChannel(guild) }.toList()

fun Args.tryTakeCategory(guild: Guild): Category? =
    mapNextString { findCategories(it, guild).singleOrNull().toMapResult() }

fun Args.takeCategory(guild: Guild): Category = tryTakeCategory(guild) ?: throw IllegalStateException("argument is not a valid Category")

fun Args.takeAllCategories(guild: Guild): List<Category> = generateSequence { tryTakeCategory(guild) }.toList()
