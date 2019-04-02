package pw.aru.utils

import com.mewna.catnip.extension.AbstractExtension
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.direct

class KodeinExtension(kodein: Kodein) : AbstractExtension("kodein"), DKodein by kodein.direct