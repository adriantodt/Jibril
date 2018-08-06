package pw.aru.utils.extensions

import pw.aru.utils.Resource
import pw.aru.utils.SettableResource
import kotlin.reflect.KProperty

operator fun <T> Resource<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = resourceOrNull

operator fun <T> SettableResource<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    setResourceAvailable(value)
}
