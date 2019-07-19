package pw.aru.core.parser

class Args(val raw: String) {
    companion object {
        val SPLIT_CHARS = charArrayOf(' ', '\r', '\n', '\t')
    }

    private var remaining = raw

    fun isEmpty() = remaining.isEmpty()

    fun takeString(): String {
        val re = remaining

        val i = re.indexOfAny(SPLIT_CHARS)

        if (i == -1) {
            remaining = ""
            return re
        }

        remaining = re.substring(i).trimStart(*SPLIT_CHARS)
        return re.substring(0, i)
    }

    fun peekString(): String {
        val re = remaining
        val i = re.indexOfAny(SPLIT_CHARS)
        return if (i != -1) re.substring(0, i) else re
    }

    fun peekRemaining(): String {
        return remaining
    }

    fun matchNextString(predicate: (String) -> Boolean): Boolean {
        val args = remaining
        val i = args.indexOfAny(SPLIT_CHARS)

        val ne = if (i != -1) args.substring(0, i) else args
        val re = if (i != -1) args.substring(i).trimStart(*SPLIT_CHARS) else ""

        val p = predicate(ne)
        if (p) remaining = re

        return p
    }

    fun matchNextString(predicate: String): Boolean {
        val args = remaining
        val i = args.indexOfAny(SPLIT_CHARS)

        val ne = if (i != -1) args.substring(0, i) else args
        val re = if (i != -1) args.substring(i).trimStart(*SPLIT_CHARS) else ""

        val p = (predicate == ne)
        if (p) remaining = re

        return p
    }

    fun <T> validateMatches(vararg pairs: Pair<T, (String) -> Boolean>): Set<T> {
        val map = pairs.toMap(LinkedHashMap())
        val validKeys = LinkedHashSet<T>()

        while (true) {
            val (key) = map.entries.firstOrNull { matchNextString(it.value) } ?: return validKeys
            map.remove(key)
            validKeys.add(key)
        }
    }

    fun <T> matchFirst(vararg pairs: Pair<T, (String) -> Boolean>): T? {
        return pairs.firstOrNull { (_, v) -> matchNextString(v) }?.first
    }

    fun <T> matchFirst(pairs: List<Pair<T, (String) -> Boolean>>): T? {
        return pairs.firstOrNull { (_, v) -> matchNextString(v) }?.first
    }

    data class MapResult<T>(val result: T, val consumed: Boolean)

    fun <T> mapNextString(map: (String) -> MapResult<T>): T {
        val args = remaining
        val i = args.indexOfAny(SPLIT_CHARS)

        val ne = if (i != -1) args.substring(0, i) else args
        val re = if (i != -1) args.substring(i).trimStart(*SPLIT_CHARS) else ""

        val r = map(ne)
        if (r.consumed) remaining = re

        return r.result
    }

    fun takeAllStrings(): List<String> {
        val re = remaining
        remaining = ""
        return re.split(*SPLIT_CHARS)
    }

    fun takeRemaining(): String {
        val re = remaining
        remaining = ""
        return re
    }
}

fun <T> T?.toMapResult() = Args.MapResult(this, this != null)