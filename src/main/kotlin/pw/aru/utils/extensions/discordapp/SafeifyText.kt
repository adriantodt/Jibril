package pw.aru.utils.extensions.discordapp

import pw.aru.utils.extensions.lang.replaceEach

fun String.safeUserInput() = replaceEach(
    "@everyone" to "@\u200Beveryone",
    "@here" to "@\u200Bhere"
)