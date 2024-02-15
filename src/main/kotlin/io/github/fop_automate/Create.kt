package io.github.fop_automate

fun main(args: Array<String>) {

    verifySetup()
    initSettings(args)

    val gitClient = GitClient(
        settings.myRepoUrl,
        settings.localRepoDir
    )

    gitClient.ensureClone(url= settings.originalRepoUrl)

    gitClient.setRemote("origin", settings.myRepoUrl)
    gitClient.setRemote("upstream", settings.originalRepoUrl)

    gitClient.ensureOriginRepository(private = true)

    gitClient.copyResource("/resources/workflows/build.yml", ".github/workflows/build.yml")
    gitClient.commit("Add build workflow", ".github/workflows/build.yml")

    gitClient.updateBuildGradle()
    gitClient.commit("Update build.gradle.kts", "build.gradle.kts")

    gitClient.updateReadme()
    gitClient.commit("Update README.md", "README.md")

    gitClient.push()

}

fun GitClient.updateBuildGradle() {

    val buildGradle = this.fileContent("build.gradle.kts")
    this.setFileContent("build.gradle.kts",
        buildGradle
            .replace("studentId = null", "studentId = \"${settings.studentId}\"")
            .replace("firstName = null", "firstName = \"${settings.firstName}\"")
            .replace("lastName = null", "lastName = \"${settings.lastName}\"")
            .replace("// studentId.set(\"\")", "studentId.set(\"${settings.studentId}\")")
            .replace("// firstName.set(\"\")", "firstName.set(\"${settings.firstName}\")")
            .replace("// lastName.set(\"\")", "lastName.set(\"${settings.lastName}\")")
            .replace("studentId.set(\"\")", "studentId.set(\"${settings.studentId}\")")
            .replace("firstName.set(\"\")", "firstName.set(\"${settings.firstName}\")")
            .replace("lastName.set(\"\")", "lastName.set(\"${settings.lastName}\")")
    )

}

fun GitClient.updateReadme() {
    (this.javaClass.getResourceAsStream("/resources/README-TEMPLATE.md") ?:
        throw RuntimeException("README template not found")).bufferedReader().use {
            val contents = it.readText()

        val readme = this.fileContent("README.md")

        if (readme.contains("<!-- FOP-Automated-Readme-Links Start -->")) {
            return
        }

        this.setFileContent("README.md",
            readme + "\n\n" + contents
                .replace("{{MY_REPO}}", settings.myRepo)
                .replace("{{exc}}", settings.task.lowercase())
                .replace("{{FIRST_NAME}}", settings.firstName)
                .replace("{{LAST_NAME}}", settings.lastName)
                .replace("{{STUDENT_ID}}", settings.studentId)
                .replace("{{PDF_BASE}}", settings.pdfBase)
                .replace("{{TASK}}", settings.task)
                .replace("{{REPO_NAME}}", settings.repoNameNoSuffix)
                .replace("{{ORIGINAL_REPO}}", settings.originalRepoNoSuffix)

        )
    }
}

