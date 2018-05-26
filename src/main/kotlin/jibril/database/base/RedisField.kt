package jibril.database.base

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jibril.database.JibrilDatabase.pool
import jibril.utils.extensions.useResource
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class RedisField<T>(private val serializer: Serializer<T>, private val defaultValue: T? = null) : ReadWriteProperty<RedisObject, T> {

    override fun getValue(thisRef: RedisObject, property: KProperty<*>): T {
        return thisRef.run {
            pool.useResource {
                it.hget(remoteId(), property.name)?.let(serializer::unserialize) ?: defaultValue ?: throw IllegalStateException("Value not set.")
            }
        }
    }

    override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: T) {
        thisRef.run {
            pool.useResource {
                it.hset(remoteId(), property.name, serializer.serialize(value))
            }
        }
    }

    class Nullable<T>(private val serializer: Serializer<T?>) : ReadWriteProperty<RedisObject, T?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): T? {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)?.let(serializer::unserialize)
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: T?) {
            thisRef.run {
                pool.useResource {
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
                pool.useResource {
                    it.hget(remoteId(), property.name) ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.String) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value)
                }
            }
        }
    }

    class Int(private val defaultValue: kotlin.Int? = null) : ReadWriteProperty<RedisObject, kotlin.Int> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Int {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)?.toInt() ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Int) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class Long(private val defaultValue: kotlin.Long? = null) : ReadWriteProperty<RedisObject, kotlin.Long> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Long {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)?.toLong() ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Long) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class Double(private val defaultValue: kotlin.Double? = null) : ReadWriteProperty<RedisObject, kotlin.Double> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Double {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)?.toDouble() ?: defaultValue ?: throw IllegalStateException("Value not set.")
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Double) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    //Strings and Primitives: NULLABLE EDITION

    class NullableString : ReadWriteProperty<RedisObject, kotlin.String?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.String? {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.String?) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value)
                }
            }
        }
    }

    class NullableInt : ReadWriteProperty<RedisObject, kotlin.Int?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Int? {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)?.toInt()
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Int?) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class NullableLong : ReadWriteProperty<RedisObject, kotlin.Long?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Long? {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)?.toLong()
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Long?) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

    class NullableDouble : ReadWriteProperty<RedisObject, kotlin.Double?> {

        override fun getValue(thisRef: RedisObject, property: KProperty<*>): kotlin.Double? {
            return thisRef.run {
                pool.useResource {
                    it.hget(remoteId(), property.name)?.toDouble()
                }
            }
        }

        override fun setValue(thisRef: RedisObject, property: KProperty<*>, value: kotlin.Double?) {
            thisRef.run {
                pool.useResource {
                    it.hset(remoteId(), property.name, value.toString())
                }
            }
        }
    }

}

