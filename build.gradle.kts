import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import proguard.gradle.ProGuardTask

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.github.johnrengelman:shadow:8.1.1")
        classpath("com.guardsquare:proguard-gradle:7.4.2")
    }
}


plugins {
    kotlin("jvm") version "1.9.22"
    application
}

apply(plugin="com.github.johnrengelman.shadow")
//apply(plugin="com.guardsquare.proguard")
group = "com.github.fop-automate"
version = "0.1.0"

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
    mainClass.set("io.github.fop_automate.create.CreateKt")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.withType(ShadowJar::class.java) {
    archiveBaseName.set("worker")
    archiveClassifier.set("")
    archiveVersion.set(version.toString())
    minimize {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }
}
//
// ProGuard Task Configuration
tasks.register<ProGuardTask>("proguard") {
    // Basic configuration for ProGuard
    // You will need to adjust it according to your project's specific needs
    configurationFiles.add(file("proguard-rules.pro"))
    injars(files("build/libs/worker-${version}.jar"))
    outjars(files("build/libs/worker-${version}-min.jar"))

    // Automatically handle the Java version of this build
    val javaVersion = System.getProperty("java.version")
    if (javaVersion.startsWith("1.")) {
        // Before Java 9, the runtime classes were packaged in a single jar file
        libraryjars("${System.getProperty("java.home")}/lib/rt.jar")
    } else {
        // As of Java 9, the runtime classes are packaged in modular jmod files
        libraryjars(mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"), file("${System.getProperty("java.home")}/jmods/java.base.jmod"))
        libraryjars(mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"), file("${System.getProperty("java.home")}/jmods/java.sql.jmod"))
        // Add more jmods if necessary
    }

    dontwarn()

}

tasks.build {
    finalizedBy("proguard")
}
