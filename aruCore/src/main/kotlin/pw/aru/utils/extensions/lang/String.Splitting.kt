@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Extensions")
@file:JvmMultifileClass

package pw.aru.utils.extensions.lang

import java.util.*


fun String.smartSplit(
    maxLength: Int = 2000,
    vararg policy: SplitPolicy = arrayOf(SplitPolicy.ANYWHERE)
): Sequence<String> {
    if (length <= maxLength) {
        return sequenceOf(this)
    }

    //val policy : Array<out SplitPolicy> = if (policy.isEmpty()) arrayOf<SplitPolicy>(SplitPolicy.ANYWHERE)

    val parts = LinkedList<String>()

    var currentBeginIndex = 0

    messageLoop@ while (currentBeginIndex < length - (maxLength + 1)) {
        for (i in policy.indices) {
            val currentEndIndex = policy[i].nextSplit(currentBeginIndex, this, maxLength)
            if (currentEndIndex != -1) {
                parts.add(substring(currentBeginIndex, currentEndIndex))
                currentBeginIndex = currentEndIndex
                continue@messageLoop
            }
        }
        throw IllegalStateException("Failed to split the messages")
    }

    if (currentBeginIndex < length - 1) {
        parts.add(substring(currentBeginIndex, length - 1))
    }

    return parts.asSequence()
}

interface SplitPolicy {

    fun nextSplit(currentBeginIndex: Int, string: String, maxLength: Int): Int

    companion object {
        val NEWLINE: SplitPolicy =
            CharSequenceSplitPolicy("\n", true)

        val SPACE: SplitPolicy =
            CharSequenceSplitPolicy(" ", true)

        val ANYWHERE: SplitPolicy = AnywhereSplitPolicy

        fun onChars(chars: String, remove: Boolean): SplitPolicy {
            return CharSequenceSplitPolicy(chars, remove)
        }
    }
}

internal object AnywhereSplitPolicy : SplitPolicy {
    override fun nextSplit(currentBeginIndex: Int, string: String, maxLength: Int): Int {
        return Math.min(currentBeginIndex + maxLength, string.length)
    }
}

internal class CharSequenceSplitPolicy(private val chars: String, private val remove: Boolean) :
    SplitPolicy {

    override fun nextSplit(currentBeginIndex: Int, string: String, maxLength: Int): Int {
        val currentEndIndex = string.substring(
            currentBeginIndex,
            currentBeginIndex + maxLength - if (this.remove) this.chars.length else 0
        ).lastIndexOf(chars)

        return if (currentEndIndex < 0) -1 else currentEndIndex + this.chars.length
    }
}