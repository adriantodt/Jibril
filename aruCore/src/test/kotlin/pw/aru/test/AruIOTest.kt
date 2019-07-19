package pw.aru.test

import mu.KotlinLogging.logger
import pw.aru.db.AruDB
import pw.aru.sides.AruSide

fun main() {
    val log = logger {}

    val mainIO = AruDB(AruSide.MAIN, 0).io()
    val devIO = AruDB(AruSide.DEV, 0).io()

    mainIO.configure {
        feed(AruSide.DEV, "hi") {
            log.info { "mainIO received hi" }
            mainIO.sendFeed("hello")
            log.info { "mainIO sent hello" }
        }

        feed(AruSide.DEV, "bye") {
            log.info { "mainIO received bye" }
        }
    }

    devIO.configure {
        feed(AruSide.MAIN, "hello") {
            log.info { "devIO received hello" }
            devIO.sendFeed("bye")
            log.info { "devIO sent bye" }
            System.exit(0)
        }
    }

    log.info { "starting" }
    devIO.sendFeed("hi")
    log.info { "devIO sent hi" }

    //block main thread so java doesn't commit System.exit
    Object().let { synchronized(it) { it.wait() } }
}