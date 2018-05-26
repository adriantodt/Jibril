package jibril.commands.utils

import jibril.core.categories.Categories
import jibril.core.categories.Category
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.BotUtils.capitalize
import jibril.utils.Colors
import jibril.utils.extensions.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.lang.reflect.Modifier
import javax.imageio.ImageIO
import kotlin.math.absoluteValue

@Command("color")
class ColorCommand : ICommand {
    override val category: Category = Categories.UTILS

    override fun call(event: GuildMessageReceivedEvent, allArgs: String) {
        val args = allArgs.split(' ', limit = 2)

        if (args.isEmpty()) return showHelp()

        val color: Color = when (args[0]) {
            "rgb" -> {
                if (args.size != 2) return showHelp()
                val arg = args[1]

                if (arg.startsWith("#") || arg.startsWith("0x")) {
                    Color.decode(arg)
                } else if (arg.contains(" ") || arg.contains(",")) {
                    val parts = arg.split(' ', ',').filter(String::isNotBlank)

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
                if (args.size != 2) return showHelp()

                val parts = args[1].split(' ', ',').filter(String::isNotBlank)

                if (parts.size != 3) return showHelp()

                try {
                    val (h, s, v) = parts.map { it.toFloat() }
                    Color.getHSBColor(h, s, v)
                } catch (_: IllegalArgumentException) {
                    return showHelp()
                }
            }
            "random" -> {
                val r = threadLocalRandom()

                Color.getHSBColor(r.nextFloat(), r.nextFloat(), r.nextFloat())
            }
            else -> {
                if (allArgs.startsWith("#") || allArgs.startsWith("0x")) {
                    Color.decode(allArgs)
                } else {
                    val colors = listOf(classOf<Color>(), classOf<Colors>())
                        .flatMap { it.fields.asIterable() }
                    val colorField = colors
                        .firstOrNull {
                            it.type == Color::class.java && (it.name.equals(allArgs, true) || it.name.equals(args[0], true))
                        }
                    colorField?.get(null) as? Color ?: return showHelp()
                }
            }
        }

        val rgbHex = "0x${(color.rgb and 0xffffff).toString(16)}"
        val file = "$rgbHex.png"

        val embed = embed {
            setColor(color)
            setTitle("Here's your color!")
            setThumbnail("attachment://$file")

            val rgb1 = color.run { "rgb(red = $red, green = $green, blue = $blue)" }
            val rgb2 = color.getRGBColorComponents(null)
                .let { (r, g, b) -> "rgb(red = ${r.format("%.3f")}, green = ${g.format("%.3f")}, blue = ${b.format("%.3f")})" }
            val hsv = color.run { Color.RGBtoHSB(red, green, blue, null) }
                .let { (h, s, v) -> "hsv(hue = ${h.format("%.3f")}, sat = ${s.format("%.3f")}, val = ${v.format("%.3f")})" }

            val name = colorLookup.getOrElse(color) {
                val (h, s, v) = color.run { Color.RGBtoHSB(red, green, blue, null) }

                val closest = colors.values.minBy {
                    val (h1, s1, v1) = it.run { Color.RGBtoHSB(red, green, blue, null) }
                    ((h - h1).absoluteValue + (s - s1).absoluteValue + (v - v1).absoluteValue) / 3
                }

                if (closest == null) null else "${colorLookup[closest]}-alike (#${(closest.rgb and 0xffffff).toString(16)})"
            }

            description(
                if (name != null) "**Name**: ${capitalize(name)}" else "",
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
        }

        event.channel.sendMessage(embed).addFile(generate(color), file).queue()
    }

    private fun generate(color: Color): ByteArray? {
        val image = BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB)

        with(image.createGraphics()) {
            paint = color
            fillRect(0, 0, 300, 300)
        }

        return ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
    }

    val colors = listOf(classOf<Color>(), classOf<Colors>())
        .flatMap { it.fields.asIterable() }
        .filter { Modifier.isStatic(it.modifiers) && it.type == Color::class.java }
        .map { it.name.let { if (it == it.toUpperCase()) it.toLowerCase() else it } to it.get(null) as Color }
        .distinct()
        .toMap(LinkedHashMap())

    val colorLookup = colors.entries.groupBy({ it.value }, { it.key }).mapValues { it.value[0] }
}