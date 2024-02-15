package io.github.fop_automate

import io.github.fop_automate.api.GitClient
import io.github.fop_automate.api.executeCommand
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File


class TempRepository(
    val name: String,
) : AutoCloseable {

    val owner = "FOP-Automate"
    val repo = "$owner/$name"
    val url = "https://github.com/$repo.git"

    fun exists() = try {
        executeCommand("gh", "repo", "view", url)
        true
    } catch (e: RuntimeException) {
        false
    }

    fun create(private: Boolean = true) {
        if (!exists())
            executeCommand(
                "gh", "repo", "create",
                url, if (private) "--private" else "--public"
            )
    }

    override fun close() {
        if (exists()) executeCommand("gh", "repo", "delete", url, "--yes")
    }

    fun withRepository(block: (String) -> Unit) {
        create(private = true)
        use {
            block(url)
        }
    }

    fun use(block: (TempRepository) -> Unit) {
        try {
            block(this)
            close()
        } catch (ex: Throwable) {
            this.close()
            throw ex
        }
    }
}

fun tempRepository(): TempRepository {
    // Create random identifier
    val identifier = (1..16).map { ('a'..'z').random() }.joinToString("")
    return TempRepository("temp-repo-$identifier")
}

fun withTempRepository(block: (TempRepository) -> Unit) {
    val repository = tempRepository()
    repository.withRepository {
        block(repository)
    }
}

fun useTempRepository(block: (TempRepository) -> Unit) {
    val repository = tempRepository()
    repository.use {
        block(it)
    }
}

class GitClientTests : FreeSpec({

    "GitClient" - {

        "constructor" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.originUrl shouldBe "https://example.com/repo.git"
            git.repository shouldBe dir
        }

        "file" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.file("file.txt").absolutePath shouldBe "${dir.absolutePath}/file.txt"
        }

        "fileContent" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.file("file.txt").writeText("Hello, World!")
            git.fileContent("file.txt") shouldBe "Hello, World!"
        }

        "setFileContent" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.setFileContent("file.txt", "Hello, World!")
            git.file("file.txt").readText() shouldBe "Hello, World!"
        }

        "executeGitCommand" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.executeGitCommand("init")
            git.file(".git").exists() shouldBe true
            git.file(".git").isDirectory shouldBe true
        }

        "init" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file(".git").exists() shouldBe true
            git.file(".git").isDirectory shouldBe true
        }

        "ensure init" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.ensureInit()
            git.ensureInit()
            git.file(".git").exists() shouldBe true
            git.file(".git").isDirectory shouldBe true
        }

        "setRemote / hasRemote / getRemoteUrl" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.hasRemote("origin") shouldBe false
            git.setRemote("origin", git.originUrl)
            git.hasRemote("origin") shouldBe true
            git.getRemoteUrl("origin") shouldBe git.originUrl
        }

        "ensure origin" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.ensureInit()
            git.ensureOrigin()
            git.hasRemote("origin") shouldBe true
            git.getRemoteUrl("origin") shouldBe git.originUrl
        }

        "add" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file.txt").writeText("Hello, World!")
            git.addFile("file.txt")
            git.executeGitCommand("status") shouldContain "file.txt"
        }

        "add of file" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file.txt").writeText("Hello, World!")
            git.addFile(git.file("file.txt"))
            git.executeGitCommand("status") shouldContain "file.txt"
        }

        "add (multiple files)" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file1.txt").writeText("Hello, World!")
            git.file("file2.txt").writeText("Hello, World!")
            git.addFiles("file1.txt", "file2.txt")
            git.executeGitCommand("status") shouldContain "file1.txt"
            git.executeGitCommand("status") shouldContain "file2.txt"
        }

        "add (multiple files) with File objects" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file1.txt").writeText("Hello, World!")
            git.file("file2.txt").writeText("Hello, World!")
            git.addFiles(git.file("file1.txt"), git.file("file2.txt"))
            git.executeGitCommand("status") shouldContain "file1.txt"
            git.executeGitCommand("status") shouldContain "file2.txt"
        }

        "commit" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file.txt").writeText("Hello, World!")
            git.addFile("file.txt")
            git.commit("Initial commit")
            git.executeGitCommand("log") shouldContain "Initial commit"
        }

        "commit file" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file.txt").writeText("Hello, World!")
            git.commit("Initial commit", "file.txt")
            git.executeGitCommand("log") shouldContain "Initial commit"
        }

        "commit files" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file1.txt").writeText("Hello, World!")
            git.file("file2.txt").writeText("Hello, World!")
            git.commit("Initial commit", "file1.txt", "file2.txt")
            git.executeGitCommand("log") shouldContain "Initial commit"
        }

        "get main branch" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.executeGitCommand("branch", "-m", "main")
            git.getMainBranch() shouldBe "main"
        }

        "is changed" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file.txt").writeText("Hello, World!")
            git.addFile("file.txt")
            git.commit("Initial commit")
            git.file("file.txt").writeText("Hello, World! 2")
            git.changed("file.txt") shouldBe true
        }

        "is file changed" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.file("file.txt").writeText("Hello, World!")
            git.addFile("file.txt")
            git.commit("Initial commit")
            git.file("file.txt").writeText("Hello, World! 2")
            git.changed(git.file("file.txt")) shouldBe true
        }

        "origin repository exists" {
            val dir = tempdir()
            val git = GitClient("https://github.com/FOP-Automate/NOT-EXISTING-REPO.git", dir)
            git.init()
            git.originRepositoryExists() shouldBe false
            git.originUrl = "https://github.com/FOP-Automate/Script.git"
            git.originRepositoryExists() shouldBe true
        }

        "create origin repository" {
            useTempRepository { repo ->
                val dir = tempdir()
                val git = GitClient(repo.url, dir)
                git.ensureInit()
                git.ensureOrigin()
                git.createOriginRepository(private = true)
                repo.exists() shouldBe true
            }
        }

        "ensure origin repository" {
            useTempRepository { repo ->
                val dir = tempdir()
                val git = GitClient(repo.url, dir)
                git.ensureInit()
                git.ensureOrigin()
                git.ensureOriginRepository(private = true)
                repo.exists() shouldBe true
            }
        }

        "push" {
            withTempRepository { repo ->
                val dir = tempdir()
                val git = GitClient(repo.url, dir)
                git.ensureInit()
                git.ensureOrigin()
                git.file("file.txt").writeText("Hello, World!")
                git.addFile("file.txt")
                git.commit("Initial commit")
                git.push()
            }
        }

        "push (mirror)" {
            withTempRepository { repo ->
                val dir = tempdir()
                val git = GitClient(repo.url, dir)
                git.ensureInit()
                git.ensureOrigin()
                git.file("file.txt").writeText("Hello, World!")
                git.addFile("file.txt")
                git.commit("Initial commit")
                git.push(mirror = true)
            }
        }

        "clone" {
            val tempdir = File(tempdir(), "clone")
            val git = GitClient("https://github.com/FOP-Automate/Script.git", tempdir)
            git.clone()
            git.file("build.gradle.kts").exists() shouldBe true
        }

        "clone (custom)" {
            val tempdir = File(tempdir(), "clone")
            val git = GitClient("https://example.com/repo.git", tempdir)
            git.clone("https://github.com/FOP-Automate/Script.git")
            git.file("build.gradle.kts").exists() shouldBe true
        }

        "ensure clone" {
            val tempdir = File(tempdir(), "clone")
            val git = GitClient("https://github.com/FOP-Automate/Script.git", tempdir)
            git.ensureClone()
            git.ensureClone()
            git.file("build.gradle.kts").exists() shouldBe true
        }

        "ensure clone (custom)" {
            val tempdir = File(tempdir(), "clone")
            val git = GitClient("https://example.com/repo.git", tempdir)
            git.ensureClone("https://github.com/FOP-Automate/Script.git")
            git.ensureClone("https://github.com/FOP-Automate/Script.git")
            git.file("build.gradle.kts").exists() shouldBe true
        }



    }

})