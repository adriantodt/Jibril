package jibril.utils.api

import jibril.core.music.MusicManager
import jibril.utils.api.impl.JibrilAPI
import net.dv8tion.jda.bot.sharding.ShardManager
import javax.inject.Inject

interface JAPIPoster {
    fun postStats()

    class Dummy : JAPIPoster {
        override fun postStats() = Unit
    }
}

class JibrilAPIStatsPoster
@Inject constructor(
    private val shardManager: ShardManager,
    private val musicManager: MusicManager,
    private val api: JibrilAPI
) : JAPIPoster {
    override fun postStats() {
        api.postStats(
            shardManager.shardsTotal.toLong(),
            shardManager.guildCache.size(),
            shardManager.userCache.size(),
            musicManager.musicPlayers.size().toLong(),
            musicManager.musicPlayers.valueCollection().map { it.queue.size.toLong() }.sum()
        ).async()
    }
}