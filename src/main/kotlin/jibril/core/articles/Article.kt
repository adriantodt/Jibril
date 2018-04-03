package jibril.core.articles

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Article(
    vararg val value: String
)