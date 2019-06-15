import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Aru!DB
plugins {
    kotlin("jvm")
    maven
    `maven-publish`
}

group = "pw.aru"
version = "1.1"

//Repositories and Dependencies
repositories {
    jcenter()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://dl.bintray.com/arudiscord/maven") }
    maven { url = uri("https://dl.bintray.com/arudiscord/kotlin") }
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    api("io.projectreactor:reactor-core:3.2.10.RELEASE")
    api("com.github.mewna:catnip:3ba7d143")
    api("io.lettuce:lettuce-core:5.1.6.RELEASE")
    api("pw.aru.libs:snowflake-local:1.0")
    api("pw.aru.libs:eventpipes:1.3.1")

    api("com.fasterxml.jackson.core:jackson-databind:2.9.9")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")

    api("org.json:json:20180813")
    api("com.squareup.okhttp3:okhttp:3.14.1")
    api("org.kodein.di:kodein-di-generic-jvm:6.1.0")

    api("ch.qos.logback:logback-classic:1.2.3")
    api("io.github.microutils:kotlin-logging:1.6.26")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "12"
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