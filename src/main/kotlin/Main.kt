package org.example

import java.io.File
import java.io.FileInputStream
import java.util.Properties

fun main(args: Array<String>) {

    val settings = Settings(
        SettingsJoin(
            listOf(
                CommandLineArgumentSettings(args),
                EnvironmentSettings(),
                PropertiesSettings(loadPropertiesFile("settings.properties"))
            )
        )
    )

}

val runtime = Runtime.getRuntime()
fun executeCommand(command: String): String {
    val process = runtime.exec(command)
    process.waitFor()
    if(process.exitValue() != 0) {
        throw RuntimeException("Command $command failed with exit code ${process.exitValue()}")
    }

    val output = process.inputStream.bufferedReader().readText()
    val error = process.errorStream.bufferedReader().readText()
    if(error.isNotEmpty()) {
        throw RuntimeException("Command $command failed with error: $error")
    }

    process.destroy()

    return output
}

fun loadPropertiesFile(path: String): Properties? {
    if(!File(path).exists()) return null
    val properties = Properties()
    properties.load(FileInputStream(path))
    return properties
}


