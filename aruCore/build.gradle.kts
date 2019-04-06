import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Aru!DB
plugins {
    kotlin("jvm")
    maven
    `maven-publish`
}

group = "pw.aru"
version = "0.3.1"

//Repositories and Dependencies
repositories {
    jcenter()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://dl.bintray.com/adriantodt/maven") }
    mavenLocal()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    compile("io.lettuce:lettuce-core:5.1.3.RELEASE")
    compile("pw.aru.snowflake:snowflake-local:1.0")
    compile("pw.aru.libs:eventpipes:1.1.1")
    compile("com.github.mewna:catnip:1.1.0")

    compile("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    compile("org.json:json:20180813")
    compile("com.squareup.okhttp3:okhttp:3.13.1")
    compile("org.kodein.di:kodein-di-generic-jvm:6.1.0")

    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("io.github.microutils:kotlin-logging:1.6.22")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java).apply {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact(sourceJar)
        }
    }
}