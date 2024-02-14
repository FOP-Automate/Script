package org.example

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
import java.io.FileInputStream
import java.util.Properties
import kotlin.system.exitProcess

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

interface SettingsSource {

    val debugEnabled: Boolean

    val studentId: String?
    val firstName: String?
    val lastName: String?
    val githubUsername: String?
    val providerGithub: String?
    val repoPrefix: String?
    val repoDirPrefix: String?

}

class CommandLineArgumentSettings(val args: Array<String>) : SettingsSource{


    override val debugEnabled: Boolean

    override val studentId: String?
    override val firstName: String?
    override val lastName: String?
    override val githubUsername: String?
    override val providerGithub: String?
    override val repoPrefix: String?
    override val repoDirPrefix: String?


    init {
        val options = Options()
        options.addOption("h", "help", false, "Prints this message")
        options.addOption("d", "debug", false, "Prints debug information")

        options.addOption("sid", "STUDENT_ID", true, "Student ID")
        options.addOption("fn", "FIRST_NAME", true, "First Name")
        options.addOption("ln", "LAST_NAME", true, "Last Name")
        options.addOption("gn", "GITHUB_USERNAME", true, "Github Username")
        options.addOption("pg", "PROVIDER_GITHUB", true, "Provider Github Username")
        options.addOption("pref", "REPO_PREFIX", true, "Repository Prefix")
        options.addOption("dpref", "REPO_DIR_PREFIX", true, "Repository Directory Prefix")

        val parser = DefaultParser()
        val cmd = parser.parse(options, args)

        if(cmd.hasOption("h")) {
            println("Usage: java -jar <jarfile> [options]")
            println("Options:")
            options.options.forEach {
                println("  -${it.opt} --${it.longOpt} : ${it.description}")
            }
            exitProcess(0)
        }

        debugEnabled = cmd.hasOption("d")
        studentId = cmd.getOptionValue("sid")
        firstName = cmd.getOptionValue("fn")
        lastName = cmd.getOptionValue("ln")
        githubUsername = cmd.getOptionValue("gn")
        providerGithub = cmd.getOptionValue("pg")
        repoPrefix = cmd.getOptionValue("pref")
        repoDirPrefix = cmd.getOptionValue("dpref")
    }
}

class EnvironmentSettings : SettingsSource {

    override val debugEnabled: Boolean = System.getenv("DEBUG")?.toBoolean() ?: false
    override val studentId: String? = System.getenv("STUDENT_ID")
    override val firstName: String? = System.getenv("FIRST_NAME")
    override val lastName: String? = System.getenv("LAST_NAME")
    override val githubUsername: String? = System.getenv("GITHUB_USERNAME")
    override val providerGithub: String? = System.getenv("PROVIDER_GITHUB")
    override val repoPrefix: String? = System.getenv("REPO_PREFIX")
    override val repoDirPrefix: String? = System.getenv("REPO_DIR_PREFIX")

}

class PropertiesSettings(val properties: Properties?): SettingsSource {

    override val debugEnabled: Boolean = false
    override val studentId: String? = properties?.getProperty("STUDENT_ID")
    override val firstName: String? = properties?.getProperty("FIRST_NAME")
    override val lastName: String? = properties?.getProperty("LAST_NAME")
    override val githubUsername: String? = properties?.getProperty("GITHUB_USERNAME")
    override val providerGithub: String? = properties?.getProperty("PROVIDER_GITHUB")
    override val repoPrefix: String? = properties?.getProperty("REPO_PREFIX")
    override val repoDirPrefix: String? = properties?.getProperty("REPO_DIR_PREFIX")
}


class SettingsJoin(
    settingsSource: List<SettingsSource>
) : SettingsSource {

    override val debugEnabled: Boolean = settingsSource.any { it.debugEnabled }
    override val studentId: String? = settingsSource.firstNotNullOfOrNull { it.studentId }
    override val firstName: String? = settingsSource.firstNotNullOfOrNull { it.firstName }
    override val lastName: String? = settingsSource.firstNotNullOfOrNull { it.lastName }
    override val githubUsername: String? = settingsSource.firstNotNullOfOrNull { it.githubUsername }
    override val providerGithub: String? = settingsSource.firstNotNullOfOrNull { it.providerGithub }
    override val repoPrefix: String? = settingsSource.firstNotNullOfOrNull { it.repoPrefix }
    override val repoDirPrefix: String? = settingsSource.firstNotNullOfOrNull { it.repoDirPrefix }

}

class Settings(
    settingsSource: SettingsSource
) {
    val debugEnabled: Boolean = settingsSource.debugEnabled
    val studentId: String = settingsSource.studentId ?: throw RuntimeException("Student ID is required")
    val firstName: String = settingsSource.firstName ?: throw RuntimeException("First Name is required")
    val lastName: String = settingsSource.lastName ?: throw RuntimeException("Last Name is required")
    val githubUsername: String = settingsSource.githubUsername ?: throw RuntimeException("Github Username is required")
    val providerGithub: String = settingsSource.providerGithub ?: throw RuntimeException("Provider Github is required")
    val repoPrefix: String = settingsSource.repoPrefix ?: throw RuntimeException("Repository Prefix is required")
    val repoDirPrefix: String = settingsSource.repoDirPrefix ?: throw RuntimeException("Repository Directory Prefix is required")
}


