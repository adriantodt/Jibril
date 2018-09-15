package pw.aru.commands.utils

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.Colors
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.extensions.*
import java.awt.Color
import java.awt.Color.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.lang.reflect.Modifier
import javax.imageio.ImageIO
import kotlin.math.absoluteValue

@Command("color")
class ColorCmd : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.UTILS

    override fun CommandContext.call() {
        val args = parseable()

        val arg = args.takeString()
        if (arg.isEmpty()) return showHelp()

        val color: Color = when (arg) {
            "rgb" -> {
                val v = args.takeRemaining()

                if (v.isEmpty()) return showHelp()

                if (v.startsWith("#") || v.startsWith("0x")) {
                    decode(v)
                } else if (v.contains(" ") || v.contains(",")) {
                    val parts = v.split(' ', ',').filter(String::isNotBlank)

                    if (parts.size != 3) return showHelp()

                    try {
                        val (r, g, b) = parts.map { it.toFloat() }
                        Color(r, g, b)
                    } catch (_: IllegalArgumentException) {
                        try {
                            val (r, g, b) = parts.map { it.toInt() }
                            Color(r, g, b)
                        } catch (_: IllegalArgumentException) {
                            return showHelp()
                        }
                    }
                } else return showHelp()
            }
            "hsv" -> {
                val parts = args.takeRemaining().split(' ', ',').filter(String::isNotBlank)

                if (parts.size != 3) return showHelp()

                try {
                    val (h, s, v) = parts.map { it.toFloat() }
                    getHSBColor(h, s, v)
                } catch (_: IllegalArgumentException) {
                    return showHelp()
                }
            }
            "random" -> {
                with(threadLocalRandom()) { getHSBColor(nextFloat(), nextFloat(), nextFloat()) }
            }
            "member" -> {
                author.color ?: white
            }
            else -> {
                if (arg.startsWith("#") || arg.startsWith("0x")) {
                    try {
                        decode(arg)
                    } catch (e: Exception) {
                        return showHelp()
                    }
                } else {
                    val raw = args.raw
                    val members = FinderUtil.findMembers(raw, guild)

                    if (members.isNotEmpty()) {
                        if (members.size > 1) return send(
                            arrayOf(
                                "$THINKING Well, I found too many users. How about refining your search?",
                                "**Users found**: ${members.joinToString(", ") { it.user.discordTag }}"
                            ).joinToString("\n")
                        ).queue()

                        members.first().color ?: white
                    } else {
                        val colors = listOf(classOf<Color>(), classOf<Colors>())
                            .flatMap { it.fields.asIterable() }
                        val colorField = colors
                            .firstOrNull {
                                it.type == Color::class.java && (it.name.equals(raw, true) || it.name.equals(raw, true))
                            }
                        colorField?.get(null) as? Color ?: return showHelp()
                    }
                }
            }
        }

        val rgbHex = "0x${(color.rgb and 0xffffff).toString(16)}"
        val file = "$rgbHex.png"

        sendEmbed {
            setColor(color)
            setTitle("Here's your color!")
            setThumbnail("attachment://$file")

            val rgb1 = color.run { "rgb(red = $red, green = $green, blue = $blue)" }
            val rgb2 = color.getRGBColorComponents(null)
                .let { (r, g, b) -> "rgb(red = ${r.format("%.3f")}, green = ${g.format("%.3f")}, blue = ${b.format("%.3f")})" }
            val hsv = color.run { RGBtoHSB(red, green, blue, null) }
                .let { (h, s, v) -> "hsv(hue = ${h.format("%.3f")}, sat = ${s.format("%.3f")}, val = ${v.format("%.3f")})" }

            val name = colorLookup.getOrElse(color) {
                val (h, s, v) = color.run { RGBtoHSB(red, green, blue, null) }

                val closest = colors.values.minBy {
                    val (h1, s1, v1) = it.run { RGBtoHSB(red, green, blue, null) }
                    ((h - h1).absoluteValue + (s - s1).absoluteValue + (v - v1).absoluteValue) / 3
                }

                if (closest == null) null else "${colorLookup[closest]}-alike (#${(closest.rgb and 0xffffff).toString(16)})"
            }

            description(
                if (name != null) "**Name**: ${name.capitalize()}" else "",
                "**Representations**:",
                "```kotlin",
                "//RGB as hex",
                "rgb(hex = $rgbHex)",

                "\n//RGB as bytes",
                rgb1,

                "\n//RGB as decimals",
                rgb2,

                "\n//HSV",
                hsv,
                "```"
            )
        }.addFile(generate(color), file).queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("color"), "Color Command"),
        Description("Returns the visual representation of the specified color."),
        Usage(
            CommandUsage("color #<hex code>", "Parses the hex-code of the color."),
            CommandUsage("color rgb <red> <green> <blue>", "Parses a color in RGB format."),
            CommandUsage("color hsv <hue> <saturation> <value>", "Parses a color in HSV format."),
            CommandUsage("color random", "Returns a random color."),
            CommandUsage("color member", "Returns your color."),
            CommandUsage("color <mention/nickname/name[#discriminator]>", "Returns the member's color.")
        )
    )

    private fun generate(color: Color): ByteArray {
        val image = BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB)

        with(image.createGraphics()) {
            paint = color
            fillRect(0, 0, 300, 300)
        }

        return ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
    }

    private val colors = sequenceOf(classOf<Color>(), classOf<Colors>())
        .flatMap { it.fields.asSequence() }
        .filter { Modifier.isStatic(it.modifiers) && it.type == Color::class.java }
        .map { f -> f.name.let { if (it == it.toUpperCase()) it.toLowerCase() else it } to f.get(null) as Color }
        .distinct()
        .toList()
        .toMap(LinkedHashMap())

    private val colorLookup = colors.entries.groupBy({ it.value }, { it.key }).mapValues { it.value[0] }
}