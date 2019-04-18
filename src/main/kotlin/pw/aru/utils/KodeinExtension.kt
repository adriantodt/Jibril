package pw.aru.utils

import com.mewna.catnip.extension.AbstractExtension
import org.kodein.di.*

class KodeinExtension(kodein: Kodein) :
    AbstractExtension("kodein"),
    KodeinAware by kodein,
    DKodeinAware by kodein.direct