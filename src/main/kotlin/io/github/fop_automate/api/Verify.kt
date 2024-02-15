package io.github.fop_automate.api

fun verifySetup() {
    verifyInstallations()
    verifyGHLogin()
}

fun verifyInstallations() {
    verifyGitInstallation()
    verifyGHInstallation()
}

fun verifyGitInstallation() {
    try {
        executeCommand("git", "--version")
    } catch (e: Exception) {
        throw Exception("Error with git installation, please verify if git is installed and in the PATH", e)
    }
}

fun verifyGHInstallation() {
    try {
        executeCommand("gh", "--version")
    } catch (e: Exception) {
        throw Exception("Error with gh cli installation, please verify if gh is installed and in the PATH", e)
    }
}

fun verifyGHLogin() {
    try {
        executeCommand("gh", "auth", "status")
    } catch (e: Exception) {
        throw Exception("Error with gh cli login, please verify if you are logged in with gh", e)
    }
}