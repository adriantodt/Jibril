@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions.lib

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo

operator fun AudioTrackInfo.component1(): String = title
operator fun AudioTrackInfo.component2(): String = author
operator fun AudioTrackInfo.component3(): Long = length
operator fun AudioTrackInfo.component4(): String = identifier
operator fun AudioTrackInfo.component5(): Boolean = isStream
operator fun AudioTrackInfo.component6(): String = uri
