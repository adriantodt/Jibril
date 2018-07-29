package pw.aru.core.parser

class Args(val raw: String) {
    private var remaining = raw

    fun isEmpty() = remaining.isEmpty()

    fun takeString(): String {
        val re = remaining
        val i = re.indexOfAny(charArrayOf(' ', '\r', '\n', '\t'))

        if (i == -1) {
            remaining = ""
            return re
        }

        remaining = re.substring(i).trimStart()
        return re.substring(0, i)
    }

    fun peekString(): String {
        val re = remaining
        val i = re.indexOfAny(charArrayOf(' ', '\r', '\n', '\t'))
        return if (i != -1) re.substring(0, i) else re
    }

    fun matchNextString(predicate: (String) -> Boolean): Boolean {
        val args = remaining
        val i = args.indexOfAny(charArrayOf(' ', '\r', '\n', '\t'))

        val ne = if (i != -1) args.substring(0, i) else args
        val re = if (i != -1) args.substring(i).trimStart() else ""

        val p = predicate(ne)
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

    fun <T> mapNextString(map: (String) -> Pair<T, Boolean>): T {
        val args = remaining
        val i = args.indexOfAny(charArrayOf(' ', '\r', '\n', '\t'))

        val ne = if (i != -1) args.substring(0, i) else args
        val re = if (i != -1) args.substring(i).trimStart() else ""

        val (t, r) = map(ne)
        if (r) remaining = re

        return t
    }

    fun takeAllStrings(): List<String> {
        val re = remaining
        remaining = ""
        return re.split(' ', '\r', '\n', '\t')
    }

    fun takeRemaining(): String {
        val re = remaining
        remaining = ""
        return re
    }

    fun takeStrings() = descontructed(Args::takeString)

    fun <T> descontructed(f: Args.() -> T) = Descontructed(f)

    inner class Descontructed<T>(private val f: Args.() -> T) {
        operator fun get(amount: Int) = (0 until amount).map { f() }
        operator fun component0() = f()
        operator fun component1() = f()
        operator fun component2() = f()
        operator fun component3() = f()
        operator fun component4() = f()
        operator fun component5() = f()
        operator fun component6() = f()
        operator fun component7() = f()
        operator fun component8() = f()
        operator fun component9() = f()
        operator fun component10() = f()
        operator fun component11() = f()
        operator fun component12() = f()
        operator fun component13() = f()
        operator fun component14() = f()
        operator fun component15() = f()
        operator fun component16() = f()
        operator fun component17() = f()
        operator fun component18() = f()
        operator fun component19() = f()
    }
}
