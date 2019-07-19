package pw.aru.utils.extensions.lib

import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

fun Kodein.MainBuilder.bindSelf() {
    bind<Kodein>() with singleton { kodein }
    bind<DKodein>() with singleton { dkodein }
}