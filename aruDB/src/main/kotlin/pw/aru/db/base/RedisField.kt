package pw.aru.db.base

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import pw.aru.utils.extensions.useResource
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class RedisField<T>(private val serializer: Serializer<T>, private val defaultValue: T? = null) : ReadWriteProperty<RedisObject, T> {

    override fun getValue(thisRef: RedisObject, property: KProperty<*>): T {
        return thisRef.run {
            db.pool.useResource {
                it.hget(remoteId(), property.name)?.let(serializer::unserialize) ?: defaultValue ?: throw IllegalStateException("Value not set.")
            }
        }
    }

    override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: T) {
        thisRef.run {
            db.pool.useResource {
                it.hset(remoteId(), property.name, serializer.serialize(value))
            }
        }
    }

    class Nullable<T>(private val serializer: Serializer<T?>) : ReadWriteProperty<RedisObject, T?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): T? {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.let(serializer::unserialize)
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: T?) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, serializer.serialize(value))
                }
            }
        }

    }

    // Jackson support

    class Jackson<T>(mapper: ObjectMapper, type: TypeReference<T>, defaultValue: T? = null) : RedisField<T>(Serializer.jackson(mapper, type), defaultValue)

    //Strings and Primitives

    class String(private val defaultValue: kotlin.String? = null) : ReadWriteProperty<RedisObject, kotlin.String> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.String {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name) ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.String) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value)
                }
            }
        }
    }

    class Int(private val defaultValue: kotlin.Int? = null) : ReadWriteProperty<RedisObject, kotlin.Int> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Int {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toInt() ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Int) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class Boolean(private val defaultValue: kotlin.Boolean? = null) : ReadWriteProperty<RedisObject, kotlin.Boolean> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Boolean {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toBoolean() ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Boolean) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class Long(private val defaultValue: kotlin.Long? = null) : ReadWriteProperty<RedisObject, kotlin.Long> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Long {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toLong() ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Long) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class Double(private val defaultValue: kotlin.Double? = null) : ReadWriteProperty<RedisObject, kotlin.Double> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Double {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toDouble() ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Double) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    //Strings and Primitives: NULLABLE EDITION

    class NullableString : ReadWriteProperty<RedisObject, kotlin.String?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.String? {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.String?) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value)
                }
            }
        }
    }

    class NullableInt : ReadWriteProperty<RedisObject, kotlin.Int?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Int? {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toInt()
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Int?) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class NullableBoolean : ReadWriteProperty<RedisObject, kotlin.Boolean?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Boolean? {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toBoolean()
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Boolean?) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class NullableLong : ReadWriteProperty<RedisObject, kotlin.Long?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Long? {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toLong()
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Long?) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class NullableDouble : ReadWriteProperty<RedisObject, kotlin.Double?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Double? {
            return thisRef.run {
                db.pool.useResource {
                    it.hget(remoteId(), property.name)?.toDouble()
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Double?) {
            thisRef.run {
                db.pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

}

