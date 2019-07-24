package pw.aru.db

import io.lettuce.core.RedisClient
import org.jdbi.v3.core.Jdbi
import pw.aru.core.AruSide
import pw.aru.db.modules.GlobalJdbiModule
import pw.aru.db.modules.RedisModule
import pw.aru.db.modules.ScopedJdbiModule
import pw.aru.libs.snowflake.SnowflakeConfig
import pw.aru.libs.snowflake.local.LocalGenerator
import pw.aru.utils.extensions.lang.environment
import pw.aru.utils.extensions.lib.get

class AruDB(config: Config = environmentBasedConfig()) {
    data class Config(
        val side: AruSide,
        val sideId: Long,
        val jbdiGlobalUrl: String,
        val jbdiScopedUrl: String,
        val redisUrl: String
    )

    val globalJdbi = Jdbi.create(config.jbdiGlobalUrl)
    val scopedJdbi = Jdbi.create(config.jbdiScopedUrl)
    val redisClient = RedisClient.create(config.redisUrl)
    val snowflakeWorker = aruSnowflakes[config.side.ordinal.toLong(), config.sideId]

    val globalJdbiModule = GlobalJdbiModule(this)
    val scopedJdbiModule = ScopedJdbiModule(this)
    val redisModule = RedisModule(this)

    companion object {
        val aruSnowflakes = LocalGenerator(SnowflakeConfig(1517400000000L))

        fun environmentBasedConfig(): Config {
            return Config(
                AruSide.valueOf(environment["ARUDB_SIDE"].toLowerCase()),
                environment["ARUDB_SIDE_ID"].toLong(),
                environment["ARUDB_JBDI_GLOBAL_URL"],
                environment["ARUDB_JBDI_SCOPED_URL"],
                environment["ARUDB_JBDI_REDIS_URL"]
            )
        }
    }
}