@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions.lib

import pw.aru.libs.snowflake.entities.SnowflakeDatacenter
import pw.aru.libs.snowflake.entities.SnowflakeGenerator
import pw.aru.libs.snowflake.entities.SnowflakeWorker

inline operator fun SnowflakeGenerator.get(datacenter: Long, worker: Long): SnowflakeWorker =
    getWorker(datacenter, worker)

inline operator fun SnowflakeGenerator.get(datacenter: Long): SnowflakeDatacenter = getDatacenter(datacenter)
inline operator fun SnowflakeDatacenter.get(worker: Long): SnowflakeWorker = getWorker(worker)
