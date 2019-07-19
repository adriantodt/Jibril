package pw.aru.utils.extensions.discordapp

import pw.aru.utils.extensions.lang.replaceEach
import java.util.*

private val keys = listOf("*", "_", "`", "~~").map { it to Regex.fromLiteral(it) }
private val escapes = arrayOf("*" to "\\*", "_" to "\\_", "~" to "\\~")

private data class FormatToken(val format: String, val start: Int)

fun String.stripFormatting(): String {
    //all the formatting keys to keep track of

    //find all tokens (formatting strings described above)
    val tokens = keys.asSequence()
        .flatMap { (key, p) -> p.findAll(this).map { FormatToken(key, it.range.start) } }
        .sortedBy(FormatToken::start)

    //iterate over all tokens, find all matching pairs, and add them to the list toRemove
    val stack = Stack<FormatToken>()
    val toRemove = ArrayList<FormatToken>()
    var inBlock = false
    for (token in tokens) {
        if (stack.empty() || stack.peek().format != token.format || stack.peek().start + token.format.length == token.start) {
            //we are at opening tag
            if (!inBlock) {
                //we are outside of block -> handle normally
                if (token.format == "`") {
                    //block start... invalidate all previous tags
                    stack.clear()
                    inBlock = true
                }
                stack.push(token)
            } else if (token.format == "`") {
                //we are inside of a block -> handle only block tag
                stack.push(token)
            }
        } else if (!stack.empty()) {
            //we found a matching close-tag
            toRemove.add(stack.pop())
            toRemove.add(token)
            if (token.format == "`" && stack.empty()) {
                //close tag closed the block
                inBlock = false
            }
        }
    }

    //sort tags to remove by their start-index and iteratively build the remaining string
    toRemove.sortBy(FormatToken::start)

    val out = StringBuilder()
    var currIndex = 0
    for (formatToken in toRemove) {
        if (currIndex < formatToken.start) out.append(this, currIndex, formatToken.start)
        currIndex = formatToken.start + formatToken.format.length
    }
    if (currIndex < length) out.append(substring(currIndex))
    //return the stripped text, escape all remaining formatting characters (did not have matching open/close before or were left/right of block
    return out.toString().replaceEach(*escapes)
}
