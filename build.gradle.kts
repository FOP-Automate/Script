plugins {
    kotlin("jvm") version "1.9.22"
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

dependencies {
    implementation("commons-cli:commons-cli:1.6.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}