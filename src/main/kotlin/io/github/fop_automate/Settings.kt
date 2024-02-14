package io.github.fop_automate

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.system.exitProcess

private var _settings: Settings? = null
val settings get() = _settings!!

fun initSettings(args: Array<String>) {
    val commandLineArgumentSettings = CommandLineArgumentSettings(args)
    val environmentSettings = EnvironmentSettings()
    val propertiesSettings = PropertiesSettings(loadPropertiesFile("settings.properties"))
    val settingsJoin = SettingsJoin(listOf(commandLineArgumentSettings, environmentSettings, propertiesSettings))
    _settings = Settings(settingsJoin)
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
    val task: String?
    val baseName: String?
    val baseSuffix: String?

}

class CommandLineArgumentSettings(val args: Array<String>) : SettingsSource {


    override val debugEnabled: Boolean

    override val studentId: String?
    override val firstName: String?
    override val lastName: String?
    override val githubUsername: String?
    override val providerGithub: String?
    override val repoPrefix: String?
    override val repoDirPrefix: String?
    override val task: String?
    override val baseName: String?
    override val baseSuffix: String?


    init {
        val options = Options()
        options.addOption("h", "help", false, "Prints this message")
        options.addOption("d", "debug", false, "Prints debug information")

        options.addOption("sid", "student-id", true, "Student ID")
        options.addOption("fn", "first-name", true, "First Name")
        options.addOption("ln", "last-name", true, "Last Name")
        options.addOption("gn", "github-username", true, "Github Username")
        options.addOption("pg", "provider-github", true, "Provider Github Username")
        options.addOption("pref", "repo-prefix", true, "Repository Prefix")
        options.addOption("dpref", "repo-dir-prefix", true, "Repository Directory Prefix")
        options.addOption("t", "task", true, "Task number (00, 01, 02, ...)")
        options.addOption("bn", "base-name", true, "Base name")
        options.addOption("bs", "base-suffix", true, "Base suffix")

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
        task = cmd.getOptionValue("t")
        baseName = cmd.getOptionValue("bn")
        baseSuffix = cmd.getOptionValue("bs")

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
    override val baseName: String? = System.getenv("BASE_NAME")
    override val task: String? = System.getenv("TASK")
    override val baseSuffix: String? = System.getenv("BASE_SUFFIX")

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
    override val task: String? = properties?.getProperty("TASK")
    override val baseName: String? = properties?.getProperty("BASE_NAME")
    override val baseSuffix: String? = properties?.getProperty("BASE_SUFFIX")

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
    override val task: String? = settingsSource.firstNotNullOfOrNull { it.task }
    override val baseName: String? = settingsSource.firstNotNullOfOrNull { it.baseName }
    override val baseSuffix: String? = settingsSource.firstNotNullOfOrNull { it.baseSuffix }

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
    val task: String = settingsSource.task ?: throw RuntimeException("Task is required")
    val baseName: String = settingsSource.baseName ?: throw RuntimeException("Base Name is required")
    val baseSuffix: String = settingsSource.baseSuffix ?: throw RuntimeException("Base Suffix is required")

    val repoName: String = "$baseName$task$baseSuffix"

    val myRepoName: String = "$repoPrefix$repoName"
    val localRepoDirName: String = "$repoDirPrefix$repoName"
    val localRepoDir = file(localRepoDirName)
    val myRepo: String = "$githubUsername/$myRepoName"

    val originalRepo: String = "$providerGithub/$repoName"

    val myRepoUrl: String = getRepoUrl()
    val originalRepoUrl: String = "https://github.com/$originalRepo.git"

    private fun getRepoUrl(): String {
        if(System.getenv("CI") != null) {
            val GITHUB_TOKEN = System.getenv("GITHUB_TOKEN")
                ?: throw Exception("GITHUB_TOKEN is not set, but we are in a CI environment")

            return "https://${GITHUB_TOKEN}@github.com/$myRepo.git"
        }
        else return "https://github.com/$myRepo.git"
    }

}

fun loadPropertiesFile(path: String): Properties? {
    if(!File(path).exists()) return null
    val properties = Properties()
    properties.load(FileInputStream(path))
    return properties
}