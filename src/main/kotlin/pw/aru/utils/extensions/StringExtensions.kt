package pw.aru.utils.extensions

import java.lang.Character.*
import kotlin.math.min

fun String.replaceEach(vararg list: Pair<String, String>): String {
    if (isEmpty() || list.isEmpty()) return this

    // keep track of which still have matches
    val noMoreMatchesForReplIndex = BooleanArray(list.size)

    // index on index that the match was found
    var textIndex = -1
    var replaceIndex = -1
    var tempIndex: Int

    // index of replace array that will replace the search string found
    // NOTE: logic duplicated below START
    for ((i, pair) in list.withIndex()) {
        val (search) = pair
        if (noMoreMatchesForReplIndex[i] || search.isEmpty()) continue
        tempIndex = indexOf(search)

        // see if we need to keep searching for this
        if (tempIndex == -1) {
            noMoreMatchesForReplIndex[i] = true
        } else if (textIndex == -1 || tempIndex < textIndex) {
            textIndex = tempIndex
            replaceIndex = i
        }
    }
    // NOTE: logic mostly below END

    // no search strings found, we are done
    if (textIndex == -1) return this

    var start = 0

    // get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
    var increase = 0

    // count the replacement text elements that are larger than their corresponding text being replaced
    for ((search, replacement) in list) {
        val greater = replacement.length - search.length
        // assume 3 matches
        if (greater > 0) increase += 3 * greater
    }
    // have upper-bound at 20% increase, then let Java take over
    increase = min(increase, length / 5)

    val buf = StringBuilder(length + increase)

    while (textIndex != -1) {
        for (i in start until textIndex) buf.append(this[i])
        buf.append(list[replaceIndex].second)

        start = textIndex + list[replaceIndex].first.length

        textIndex = -1
        replaceIndex = -1
        // find the next earliest match
        // NOTE: logic mostly duplicated above START
        for ((i, pair) in list.withIndex()) {
            val (search) = pair
            if (noMoreMatchesForReplIndex[i] || search.isEmpty()) continue
            tempIndex = indexOf(search, start)

            // see if we need to keep searching for this
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true
            } else if (textIndex == -1 || tempIndex < textIndex) {
                textIndex = tempIndex
                replaceIndex = i
            }
        }
        // NOTE: logic duplicated above END
    }

    for (i in start until length) buf.append(this[i])

    return buf.toString()
}

fun String.initials(): String = filter(Char::isUpperCase)
fun String.capitalize(): String = if (length < 2) this else "${toUpperCase(this[0])}${substring(1)}"

private val regexRegex = Regex("[\\-\\[\\]/{}()*+?.\\\\^$|]")
fun String.escapeRegex(): String = regexRegex.replace(this, "\\$&")

fun String.decapitalize(): String? {
    if (isEmpty()) return this

    if (length > 1 && isUpperCase(this[1]) && isUpperCase(this[0])) {
        //IO -> io; JDA -> jda
        return if (this == toUpperCase()) toLowerCase() else this
    }
    val chars = toCharArray()
    chars[0] = toLowerCase(chars[0])
    return String(chars)
}
