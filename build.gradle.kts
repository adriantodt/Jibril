import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.palantir.gradle.docker.DockerExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.github.ben-manes.versions") version "0.21.0"
    id("com.palantir.docker") version "0.21.0"
}

group = "pw.aru"
version = "3.1.3"

repositories {
    jcenter()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://dl.bintray.com/arudiscord/maven") }
    maven { url = uri("https://dl.bintray.com/arudiscord/kotlin") }
    maven { url = uri("https://dl.bintray.com/arudiscord/hg") }
    maven { url = uri("https://dl.bintray.com/adriantodt/maven") }
    maven { url = uri("https://dl.bintray.com/natanbc/maven") }
    maven { url = uri("https://dl.bintray.com/kodehawa/maven") }
    mavenLocal()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    compile(project("aruCore"))

    compile("com.github.mewna:catnip:1.3.2")
    compile("io.projectreactor.addons:reactor-adapter:3.2.3.RELEASE")
    compile("io.reactivex.rxjava2:rxkotlin:2.3.0")
    compile("io.sentry:sentry:1.7.23")

    // Main APIs
    compile("com.github.natanbc:weeb4j:3.5")
    compile("pw.aru.libs:andeclient:1.7.1")
    compile("com.sedmelluq:lavaplayer:1.3.19") {
        exclude(group = "com.sedmelluq", module = "lavaplayer-natives")
    }

    //trying last Netty
    val netty_version = "4.1.36.Final"
    compile("io.netty:netty-codec-http2:$netty_version")
    compile("io.netty:netty-handler-proxy:$netty_version")
    compile("io.netty:netty-resolver-dns:$netty_version")
    compile("io.netty:netty-transport:$netty_version")
    compile("io.netty:netty-buffer:$netty_version")
    compile("io.netty:netty-common:$netty_version")

    // Useful
    compile("com.github.natanbc:java-eval:1.0")
    compile("net.kodehawa:imageboard-api:2.0.7")

    // Open-Source Libraries
    compile("pw.aru.libs:catnip-entityfinder:1.0")
    compile("pw.aru.libs:resources:1.0")
    compile("pw.aru.libs:dice-notation:1.2")
    compile("pw.aru.libs:snowflake-local:1.0")
    compile("pw.aru.libs:DD4J:1.0")
    compile("pw.aru.hg:hg-engine:1.0")
    compile("pw.aru.hg:hg-loader:1.0")

    //Scanning and Injections
    compile("io.github.classgraph:classgraph:4.8.37")
    compile("org.kodein.di:kodein-di-generic-jvm:6.1.0")
    compile("pw.aru.libs:kodein-jit-bindings:2.2")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<ShadowJar> {
        configurations = listOf(project.configurations.runtime.get())
    }

    register("createRunDir", Copy::class) {
        from("runDir")
        into("run")
    }
}

configure<ApplicationPluginConvention> {
    mainClassName = "pw.aru.Bootstrap"
}

configure<DockerExtension> {
    this.name = "adriantodt/aru:$version"

    val shadowJar by tasks.getting

    dependsOn(shadowJar)
    files(shadowJar.outputs)
    copySpec.from("runDir").into("run")

    buildArgs(mapOf("version" to version.toString()))
}

rootProject.run {
    file("src/main/kotlin/pw/aru/exported/exported.kt").run {
        parentFile.mkdirs()
        createNewFile()
        writeText(
            """
@file:JvmName("AruExported")
@file:Suppress("unused")

/*
 * file "exported.kt". DO NOT EDIT MANUALLY. THIS FILE IS GENERATED BY GRADLE.
 */

package pw.aru.exported

/**
 * Aru! Version
 */
const val aru_version = "$version"

/**
 * User Agent
 */
const val user_agent = "Aru/Discord (Aru! $version)"
""".trim()
        )
    }

    file(".env").run {
        parentFile.mkdirs()
        createNewFile()
        writeText(
            """
aru_version=$version
""".trim()
        )
    }
}



