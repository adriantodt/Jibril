package jibril.data.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import xyz.cuteclouds.hunger.loader.write
import java.io.File

object ConfigManager {
    @Deprecated("Use Injections instead")
    val config: Config by lazy {
        try {
            mapper.readValue<Config>(file.readText())
        } catch (e: Exception) {
            file.renameTo(File("config.json.bkp"))
            save(Config())
            throw e
        }
    }

    fun save() = save(config)

    private val mapper = jacksonObjectMapper()
    private val file = File("config.json")

    private fun save(config: Config) = file.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config))
}
