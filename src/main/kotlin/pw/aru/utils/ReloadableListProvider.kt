package pw.aru.utils

import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

interface Reloadable {
    fun reload()
}

class ReloadableListProvider : Reloadable {

    private val _map = LinkedHashMap<File, WeakReference<ReloadableList>>()

    private fun fileMap() = _map.apply { values.removeIf { it.get() == null } }

    operator fun get(file: String) = get(File(file))

    operator fun get(file: File) = fileMap()[file]?.get() ?: create(file)

    private fun create(file: File): ReloadableList {
        val list = ReloadableList(file)
        fileMap()[file] = WeakReference(list)
        return list
    }

    override fun reload() {
        for (value in fileMap().values) value.get()?.reload()
    }
}

class ReloadableList(private val file: File) : CopyOnWriteArrayList<String>(), Reloadable {
    init {
        reload()
    }

    override fun reload() {
        clear()
        addAll(file.readLines())
    }
}