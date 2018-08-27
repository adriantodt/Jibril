package pw.aru.commands.actions.v2

import pw.aru.core.categories.Category

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
}

class ImageCommandBuilder(
    override val names: List<String>,
    override val category: Category?,
    override val commandName: String,
    override val description: String
) : ImageBasedCommandBuilder() {
    var messages: List<String> = emptyList()
}
