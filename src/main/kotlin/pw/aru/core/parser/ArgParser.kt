package pw.aru.core.parser

class Args(val raw: String) {
    private var remaining = raw

    fun takeString(): String {
        val re = remaining
        val i = re.indexOf(' ')

        if (i == -1) {
            remaining = ""
            return re
        }

        remaining = re.substring(i).trimStart()
        return re.substring(0, i)
    }

    fun takeRemainingStrings(): String {
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

