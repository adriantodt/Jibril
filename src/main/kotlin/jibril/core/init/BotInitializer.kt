package jibril.core.init

import com.google.inject.Injector
import jibril.core.CommandRegistry
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.extensions.classOf
import jibril.utils.extensions.invoke
import org.reflections.Reflections

class BotInitializer(reflections: Reflections) {
    constructor(basePackage: String) : this(Reflections(basePackage))

    private val commandClasses: Set<Class<out ICommand>>

    init {
        this.commandClasses = reflections.getSubTypesOf<ICommand>(classOf())
            .filterTo(HashSet()) { it.isAnnotationPresent(classOf<Command>()) }
    }

    fun initialize(injector: Injector) {
        commandClasses.forEach {
            CommandRegistry.register(it.getAnnotation<Command>(classOf()), injector(it))
        }
    }
}
