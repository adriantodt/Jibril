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

    fun matchNextString(predicate: (String) -> Boolean): Boolean {
        val args = remaining
        val i = args.indexOfAny(charArrayOf(' ', '\r', '\n', '\t'))

        val ne = if (i != -1) args.substring(0, i) else args
        val re = if (i != -1) args.substring(i).trimStart() else ""

        val p = predicate(ne)
        if (p) remaining = re

        return p
    }

    fun <T> mapNextString(map: (String) -> Pair<T?, Boolean>): T? {
        val args = remaining
        val i = args.indexOfAny(charArrayOf(' ', '\r', '\n', '\t'))

        val ne = if (i != -1) args.substring(0, i) else args
        val re = if (i != -1) args.substring(i).trimStart() else ""

        val (t, r) = map(ne)
        if (r) remaining = re

        return t
    }

    fun tryTakeInt(): Int? = mapNextString {
        try {
            it.toInt() to true
        } catch (e: NumberFormatException) {
            null to false
        }
    }

    fun tryTakeLong(): Long? = mapNextString {
        try {
            it.toLong() to true
        } catch (e: NumberFormatException) {
            null to false
        }
    }

    fun tryTakeDouble(): Double? = mapNextString {
        try {
            it.toDouble() to true
        } catch (e: NumberFormatException) {
            null to false
        }
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

    fun takeStrings() = descontructed { takeString() }

    inner class Descontructed<T>(private val f: (Args) -> T) {
        operator fun component0() = f(this@Args)
        operator fun component1() = f(this@Args)
        operator fun component2() = f(this@Args)
        operator fun component3() = f(this@Args)
        operator fun component4() = f(this@Args)
        operator fun component5() = f(this@Args)
        operator fun component6() = f(this@Args)
        operator fun component7() = f(this@Args)
        operator fun component8() = f(this@Args)
        operator fun component9() = f(this@Args)
        operator fun component10() = f(this@Args)
        operator fun component11() = f(this@Args)
        operator fun component12() = f(this@Args)
    }

    fun <T> descontructed(f: (Args) -> T) = Descontructed(f)
}

