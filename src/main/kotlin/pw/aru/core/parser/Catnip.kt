package pw.aru.core.parser

import com.mewna.catnip.entity.channel.Category
import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.channel.VoiceChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.guild.Role
import com.mewna.catnip.entity.misc.Emoji
import gg.amy.catnip.utilities.FinderUtil.*

fun Args.tryTakeMember(guild: Guild): Member? = mapNextString { findMembers(it, guild).singleOrNull().let { v -> v to (v != null) } }

fun Args.takeMember(guild: Guild): Member = tryTakeMember(guild) ?: throw IllegalStateException("argument is not a valid Member")

fun Args.takeMembers(guild: Guild) = deconstructed { takeMember(guild) }

fun Args.takeAllMembers(guild: Guild): List<Member> = generateSequence { tryTakeMember(guild) }.toList()

fun Args.tryTakeRole(guild: Guild): Role? = mapNextString { findRoles(it, guild).singleOrNull().let { v -> v to (v != null) } }

fun Args.takeRole(guild: Guild): Role = tryTakeRole(guild) ?: throw IllegalStateException("argument is not a valid Role")

fun Args.takeRoles(guild: Guild) = deconstructed { takeRole(guild) }

fun Args.takeAllRoles(guild: Guild): List<Role> = generateSequence { tryTakeRole(guild) }.toList()

fun Args.tryTakeEmoji(guild: Guild): Emoji? =
    mapNextString { findEmojis(it, guild).singleOrNull().let { v -> v to (v != null) } }

fun Args.takeEmoji(guild: Guild): Emoji =
    tryTakeEmoji(guild) ?: throw IllegalStateException("argument is not a valid Emoji")

fun Args.takeEmojis(guild: Guild) = deconstructed { takeEmoji(guild) }

fun Args.takeAllEmojis(guild: Guild): List<Emoji> = generateSequence { tryTakeEmoji(guild) }.toList()

fun Args.tryTakeTextChannel(guild: Guild): TextChannel? = mapNextString { findTextChannels(it, guild).singleOrNull().let { v -> v to (v != null) } }

fun Args.takeTextChannel(guild: Guild): TextChannel = tryTakeTextChannel(guild) ?: throw IllegalStateException("argument is not a valid TextChannel")

fun Args.takeTextChannels(guild: Guild) = deconstructed { takeTextChannel(guild) }

fun Args.takeAllTextChannels(guild: Guild): List<TextChannel> = generateSequence { tryTakeTextChannel(guild) }.toList()

fun Args.tryTakeVoiceChannel(guild: Guild): VoiceChannel? = mapNextString { findVoiceChannels(it, guild).singleOrNull().let { v -> v to (v != null) } }

fun Args.takeVoiceChannel(guild: Guild): VoiceChannel = tryTakeVoiceChannel(guild) ?: throw IllegalStateException("argument is not a valid VoiceChannel")

fun Args.takeVoiceChannels(guild: Guild) = deconstructed { takeVoiceChannel(guild) }

fun Args.takeAllVoiceChannels(guild: Guild): List<VoiceChannel> = generateSequence { tryTakeVoiceChannel(guild) }.toList()

fun Args.tryTakeCategory(guild: Guild): Category? = mapNextString { findCategories(it, guild).singleOrNull().let { v -> v to (v != null) } }

fun Args.takeCategory(guild: Guild): Category = tryTakeCategory(guild) ?: throw IllegalStateException("argument is not a valid Category")

fun Args.takeCategories(guild: Guild) = deconstructed { takeCategory(guild) }

fun Args.takeAllCategories(guild: Guild): List<Category> = generateSequence { tryTakeCategory(guild) }.toList()
