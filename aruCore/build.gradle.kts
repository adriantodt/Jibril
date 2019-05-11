import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Aru!DB
plugins {
    kotlin("jvm")
    maven
    `maven-publish`
}

group = "pw.aru"
version = "1.0"

//Repositories and Dependencies
repositories {
    jcenter()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://dl.bintray.com/arudiscord/maven") }
    maven { url = uri("https://dl.bintray.com/arudiscord/kotlin") }
    mavenLocal()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    compile("io.projectreactor:reactor-core:3.2.9.RELEASE")
    compile("com.github.mewna:catnip:1.2.3")
    compile("io.lettuce:lettuce-core:5.1.6.RELEASE")
    compile("pw.aru.libs:snowflake-local:1.0")
    compile("pw.aru.libs:eventpipes:1.3.1")

    compile("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    compile("org.json:json:20180813")
    compile("com.squareup.okhttp3:okhttp:3.14.1")
    compile("org.kodein.di:kodein-di-generic-jvm:6.1.0")

    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("io.github.microutils:kotlin-logging:1.6.26")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact(sourceJar)
        }
    }
}