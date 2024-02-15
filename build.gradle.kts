plugins {
    kotlin("jvm") version "1.9.22"
    application
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.github.johnrengelman:shadow:8.1.1")
    }
}

apply(plugin="com.github.johnrengelman.shadow")

group = "com.github.fop-automate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation("commons-cli:commons-cli:1.6.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.fop_automate.CreateKt")
}
