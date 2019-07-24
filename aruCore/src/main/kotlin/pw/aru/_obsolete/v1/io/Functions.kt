package pw.aru._obsolete.v1.io

import pw.aru._obsolete.v1.io.entities.CallResponse
import pw.aru._obsolete.v1.io.entities.CommandCall
import pw.aru._obsolete.v1.io.entities.FeedMessage

typealias FeedConsumer = (FeedMessage) -> Unit
typealias CallHandler = (CommandCall) -> CallResponse?
