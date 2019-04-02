package pw.aru.io

import pw.aru.io.entities.CallResponse
import pw.aru.io.entities.CommandCall
import pw.aru.io.entities.FeedMessage

typealias FeedConsumer = (FeedMessage) -> Unit
typealias CallHandler = (CommandCall) -> CallResponse?
