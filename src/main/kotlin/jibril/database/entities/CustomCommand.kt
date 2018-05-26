package jibril.database.entities

import jibril.core.commands.CommandPermission
import jibril.database.base.RedisField
import jibril.database.base.RedisObject
import jibril.database.base.Serializer

class CustomCommand(id: Long) : RedisObject(id) {
    var permission: CommandPermission by RedisField(Serializer.enum(), CommandPermission.USER)

    var value: String by RedisField.String()
}