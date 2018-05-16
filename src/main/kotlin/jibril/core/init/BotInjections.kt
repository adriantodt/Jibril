package jibril.core.init

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.name.Named
import com.google.inject.name.Names
import jibril.Jibril
import jibril.data.config.ConfigManager
import jibril.data.config.Webhooks
import jibril.logging.LogHook
import jibril.logging.LogHookManager
import jibril.utils.api.DBLPoster
import jibril.utils.api.DiscordBotsAPIPoster
import jibril.utils.extensions.classOf
import net.dv8tion.jda.bot.sharding.ShardManager
import java.beans.IntrospectionException
import java.beans.Introspector
import java.lang.reflect.InvocationTargetException
import javax.annotation.Nullable

class BotInjections(
    private val shardManager: ShardManager,
    private val manager: LogHookManager
) : AbstractModule() {

    override fun configure() {
        val config = ConfigManager.config

        bindInstance(shardManager)
        bindInstance(config.api)

        mapConstants(config.tokens, "token")
        mapConstants(config.channels, "channel")
        mapLoggers(config.webhooks)

        bindClass<DBLPoster>()
            .to(if (config.dev) classOf<DBLPoster.Dummy>() else classOf<DiscordBotsAPIPoster>())
            .asEagerSingleton()
    }

    @Provides
    fun provideDiscordBotsAPI(@Nullable @Named("token.discordBots") token: String?) = DiscordBotsAPI.Builder()
        .setToken(token)
        .setHttpClient(Jibril.httpClient)
        .build()

    private fun mapLoggers(obj: Webhooks) {
        try {
            for (p in Introspector.getBeanInfo(obj.javaClass).propertyDescriptors) {
                val link = p.readMethod(obj) as? String ?: continue

                bindClass<LogHook>()
                    .annotatedWith(Names.named("log.${p.name}"))
                    .toInstance(LogHook(manager, link))
            }
        } catch (e: IntrospectionException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    private fun mapConstants(obj: Any?, prefix: String) {
        try {
            for (p in Introspector.getBeanInfo(obj!!.javaClass).propertyDescriptors) {
                val result = p.readMethod(obj) ?: continue

                @Suppress("UNCHECKED_CAST")
                bind(p.propertyType as Class<Any>)
                    .annotatedWith(Names.named("$prefix.${p.name}"))
                    .toInstance(result)
            }
        } catch (e: IntrospectionException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    fun toInjector(): Injector = Guice.createInjector(this)

    private inline fun <reified T> bindClass() = bind<T>(classOf())

    private inline fun <reified T> bindInstance(t: T) = bindClass<T>().toInstance(t)
}

