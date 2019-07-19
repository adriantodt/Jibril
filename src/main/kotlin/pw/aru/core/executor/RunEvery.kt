package pw.aru.core.executor

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RunEvery(
    val initialDelay: Long = 0,
    val amount: Long,
    val unit: TimeUnit
)

