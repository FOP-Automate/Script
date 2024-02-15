package io.github.fop_automate.setup

import io.github.fop_automate.api.GitClient
import io.github.fop_automate.api.verifySetup
import java.io.File


fun main(args: Array<String>) {

    if(args.size != 1) {
        println("Usage: fop-automate setup <template-repository>")
        return
    }

    verifySetup();

    val headerMessage = "Welcome to the FOP Automate Setup Tool!"
    val headerDecoration = "#".repeat(headerMessage.length + 10)

    println(headerDecoration)
    println("##    $headerMessage    ##")
    println(headerDecoration)

    println()

    println("This tool will guide you through the setup process for FOP Automate.")

    println()

    val repositoryName = System.getenv("REPO_NAME")
        ?: readInput("How should the repository be named (remotely, on github)?")

    val repositoryDir = System.getenv("REPO_DIR")
        ?: readInput("How should the repository be named (locally, on your computer)?")

    val repository = GitClient(
        repositoryName,
        File(repositoryDir).absoluteFile
    )

    if (repository.exists()) {
        println("The repository already exists. Please delete it and try again.")
        return
    }

    repository.clone(args[0])

    repository.executeGitCommand("remote", "rename", "origin", "upstream")

    val githubUsername = System.getenv("GITHUB_USERNAME")
        ?: readInput("What is your GitHub username?")

    fun getRepoUrl(): String {
        if(System.getenv("CI") != null) {
            val GITHUB_TOKEN = System.getenv("GITHUB_TOKEN")
                ?: throw Exception("GITHUB_TOKEN is not set, but we are in a CI environment")

            return "https://${GITHUB_TOKEN}@github.com/$githubUsername/$repositoryName.git"
        }
        else return "https://github.com/$githubUsername/$repositoryName.git"
    }

    repository.originUrl = getRepoUrl()
    repository.setRemote("origin", repository.originUrl)

    repository.ensureOriginRepository(private = true)

    // get data for settings
    val studentId = System.getenv("STUDENT_ID")
        ?: readInput("What is your student ID?")

    val firstName = System.getenv("FIRST_NAME")
        ?: readInput("What is your first name?")

    val lastName = System.getenv("LAST_NAME")
        ?: readInput("What is your last name?")

    // Append all this data to settings.properties
    val file = File("settings.properties")
    if (!file.exists()) {
        file.createNewFile()
    }
    val lines = file.readLines().toMutableList()
    lines.add("STUDENT_ID=$studentId")
    lines.add("FIRST_NAME=$firstName")
    lines.add("LAST_NAME=$lastName")
    lines.add("GITHUB_USERNAME=$githubUsername")
    file.writeText(lines.joinToString(System.lineSeparator()) + System.lineSeparator())
}

fun readInput(prompt: String): String {
    while (true) {
        print("$prompt: ")
        val input = readlnOrNull() ?: continue

        print("You entered: $input. Is this correct? (y/n): ")
        val confirmation = readlnOrNull() ?: continue

        if (confirmation.lowercase() == "y") {
            return input
        }

        println("Please try again.")

    }
}