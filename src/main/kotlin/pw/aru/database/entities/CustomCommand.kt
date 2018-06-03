package pw.aru.database.entities

import pw.aru.core.commands.CommandPermission
import pw.aru.database.base.RedisField
import pw.aru.database.base.RedisObject
import pw.aru.database.base.Serializer

class CustomCommand(id: Long) : RedisObject(id) {
    var permission: CommandPermission by RedisField(Serializer.enum(), CommandPermission.USER)

    var value: String by RedisField.String()
}