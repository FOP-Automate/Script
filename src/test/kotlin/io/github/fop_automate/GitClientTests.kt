package io.github.fop_automate

import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class GitClientTests : FreeSpec ({

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

        "setRemote / hasRemote / getRemoteUrl" {
            val dir = tempdir()
            val git = GitClient("https://example.com/repo.git", dir)
            git.init()
            git.hasRemote("origin") shouldBe false
            git.setRemote("origin", git.originUrl)
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


    }

})