@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Extensions")
@file:JvmMultifileClass

package pw.aru.utils.extensions.lang

import kotlin.math.min

fun String.replaceEach(vararg list: Pair<String, String>): String {
    if (isEmpty() || list.isEmpty()) return this

    // keep track of which still have matches
    val noMoreMatchesForReplIndex = BooleanArray(list.size)

    // index on index that the match was found
    var textIndex = -1
    var replaceIndex = -1
    var tempIndex: Int
    var start = 0

    // index of replace array that will replace the search string found
    fun doNext() {
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
    }

    doNext()

    // no search strings found, we are done
    if (textIndex == -1) return this

    // get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
    // count the replacement text elements that are larger than their corresponding text being replaced
    // have upper-bound at 20% increase, then let Java take over
    val buf = StringBuilder(
        length + min(length / 5, 3 * list.sumBy { (s, r) -> min(r.length - s.length, 0) })
    )

    while (textIndex != -1) {
        for (i in start until textIndex) buf.append(this[i])
        val (replaced, replacement) = list[replaceIndex]
        buf.append(replacement)

        start = textIndex + replaced.length

        textIndex = -1
        replaceIndex = -1

        doNext()
    }

    buf.append(this, start, length)

    return buf.toString()
}