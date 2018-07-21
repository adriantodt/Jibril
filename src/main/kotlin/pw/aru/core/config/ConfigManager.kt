package pw.aru.core.config

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import pw.aru.utils.Properties
import xyz.cuteclouds.hunger.loader.write
import java.io.File
import java.io.FileNotFoundException

object ConfigManager {
    val config: AruConfig by lazy {
        try {
            mapper.convertValue<AruConfig>(Properties.fromFile(file))
        } catch (e: Exception) {
            if (e !is FileNotFoundException) file.renameTo(backupFile)
            save(AruConfig())
            throw e
        }
    }

    fun save() = save(config)

    private val mapper = jacksonObjectMapper()

    private val file = File("aru.properties")
    private val backupFile = File("aru.properties.bkp")

    private fun save(config: AruConfig) = file.write(
        Properties().apply {
            putAll(mapper.convertValue<Map<String, String>>(config))
        }.storeToString("Aru Config")
    )
}
