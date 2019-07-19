package pw.aru.commands.developer

import pw.aru.bot.CommandRegistry
import pw.aru.bot.categories.Category
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.help.*
import pw.aru.commands.actions.impl.ImageBasedCommandImpl
import pw.aru.core.permissions.UserPermissions
import pw.aru.exported.aru_version
import pw.aru.utils.extensions.lang.plusAssign
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ContentGenerator(private val registry: CommandRegistry) {

    fun generateCmdYaml(): String {
        val cmdsGroup = registry.lookup.entries.groupBy { it.key.category }
        val builder = StringBuilder()
        for (category in Category.LIST) {
            if (category.nsfw || category.permissions.contains(UserPermissions.BOT_DEVELOPER)) continue
            val cmds = cmdsGroup[category] ?: continue
            if (cmds.isEmpty()) continue

            builder += "- name: ${category.categoryName}\n  list:\n"
            for ((cmd, cmdNames) in cmds) {
                builder += "    - cmd: ${cmdNames[0]}\n"
                if (cmdNames.size > 1) {
                    builder += "      alias: ${cmdNames.drop(1).joinToString(" ")}\n"
                }

                builder += when (cmd) {
                    is ImageBasedCommandImpl -> "      desc: |-\n        ${cmd.description.replace(
                        "\n",
                        "\n        "
                    )}\n"
                    is ICommand.HelpDialogProvider -> {
                        val handler = cmd.helpHandler
                        "      desc: |-\n        ${
                        if (handler is Help) (handler.nodes.firstOrNull { it is Description } as? Description)?.value
                            ?.replace("\n", "\n        ")
                            ?: "TODO" else "TODO"
                        }\n"
                    }
                    else -> "      desc: TODO\n"
                }

                builder += "\n"
            }
            builder += "\n"
        }

        return builder.toString()
    }

    fun generateCmdZip(): ByteArray {
        val cmdsGroup = registry.lookup.entries.groupBy { it.key.category }

        val output = ByteArrayOutputStream()
        val zip = ZipOutputStream(output)

        for (category in Category.LIST) {
            if (category.nsfw || category.permissions.contains(UserPermissions.BOT_DEVELOPER)) continue
            val cmds = cmdsGroup[category] ?: continue
            if (cmds.isEmpty()) continue

            val categoryName = category.name.toLowerCase()

            for ((cmd, cmdNames) in cmds) {
                val help = (cmd as? ICommand.HelpDialogProvider)?.helpHandler as? Help ?: continue

                val builder = StringBuilder()

                builder += "# ${help.title}\n"

                for (node in help.nodes) when (node) {
                    is Description -> {
                        builder += "\n## Description:\n"
                        builder += node.value.replace("\n", "\n\n")
                        builder += "\n"
                    }
                    is Usage -> {
                        builder += "\n## Usage:\n"
                        builder += node.nodes.joinToString("\n\n")
                        builder += "\n"
                    }
                    is Example -> {
                        builder += "\n## Example:\n"
                        builder += node.displayValues.joinToString(
                            prefix = "```\n",
                            separator = "\n",
                            postfix = "\n```"
                        )
                        builder += "\n"
                    }
                    is Note -> {
                        builder += "\n## Note:\n"
                        builder += node.value.replace("\n", "\n\n")
                        builder += "\n"
                    }
                    is SeeAlso -> {
                        builder += "\n## See Also:\n"
                        builder += node.value.replace("\n", "\n\n")
                        builder += "\n"
                    }
                    is Field -> {
                        builder += "\n## ${node.name}:\n"
                        builder += node.value.replace("\n", "\n\n")
                        builder += "\n"
                    }
                }

                zip.putNextEntry(ZipEntry("$categoryName/${cmdNames[0]}.md"))
                zip.write(builder.toString().toByteArray())
                zip.closeEntry()
            }
        }

        zip.close()

        return output.toByteArray()
    }

    fun generateCmdHtml(): String {
        val cmdsGroup = registry.lookup.entries.groupBy({ it.key.category }, { it.value[0] })
        val builder = StringBuilder()
        builder += "<p class=\"fmt-h4\">My Commands: (v$aru_version)</p>\n<ul class=\"bot-list\">\n"

        for (category in Category.LIST) {
            if (category.nsfw || category.permissions.contains(UserPermissions.BOT_DEVELOPER)) continue
            val cmds = cmdsGroup[category] ?: continue
            if (cmds.isEmpty()) continue

            builder += "<li><b class=\"fmt-b\">${category.categoryName}</b>: ${cmds.sorted().joinToString(
                "</code> <code>",
                "<code>",
                "</code>"
            )}</li>\n"
        }

        builder += "</ul>\n"
        return builder.toString()
    }

    fun generateCmdMd(): String {
        val cmdsGroup = registry.lookup.entries.groupBy({ it.key.category }, { it.value[0] })
        val builder = StringBuilder()
        builder += "### My Commands: (v$aru_version)\n\n"

        for (category in Category.LIST) {
            if (category.nsfw || category.permissions.contains(UserPermissions.BOT_DEVELOPER)) continue
            val cmds = cmdsGroup[category] ?: continue
            if (cmds.isEmpty()) continue

            builder += "- **${category.categoryName}**: ${cmds.sorted().joinToString("` `", "`", "`")}\n"
        }

        builder += "\n"
        return builder.toString()
    }
}