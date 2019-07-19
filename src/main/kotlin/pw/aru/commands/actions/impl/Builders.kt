package pw.aru.commands.actions.impl

import pw.aru.bot.categories.Category

sealed class ImageBasedCommandBuilder {
    abstract val names: List<String>
    abstract val category: Category?
    abstract val commandName: String
    abstract val description: String

    var provider: ImageProvider? = null
    var nsfwProvider: ImageProvider? = null
    var note: String? = null
}

class ActionCommandBuilder(
    override val names: List<String>,
    override val category: Category?,
    override val commandName: String,
    override val description: String
) : ImageBasedCommandBuilder() {
    var anyTarget: String = ""
    var noTargets: String = ""
    var targetsYou: String = ""
    var targetsMe: String = ""

    fun actions(anyTarget: String, noTargets: String, targetsYou: String, targetsMe: String) {
        this.anyTarget = anyTarget
        this.noTargets = noTargets
        this.targetsYou = targetsYou
        this.targetsMe = targetsMe
    }
}

class ImageCommandBuilder(
    override val names: List<String>,
    override val category: Category?,
    override val commandName: String,
    override val description: String
) : ImageBasedCommandBuilder() {
    var messages: List<String> = emptyList()

    fun messages(vararg values: String) {
        messages = values.toList()
    }
}
