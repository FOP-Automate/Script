package io.github.fop_automate

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

class CommandLineArgumentSettingsTest : FreeSpec({

    "CommandLineArgumentSettings" - {
        "should correctly parse debug flag" {
            val args = arrayOf("-d")
            val settings = CommandLineArgumentSettings(args)
            settings.debugEnabled shouldBe true
        }

        "should correctly parse student ID" {
            val args = arrayOf("--student-id", "12345")
            val settings = CommandLineArgumentSettings(args)
            settings.studentId shouldBe "12345"
        }

        "should correctly parse first name" {
            val args = arrayOf("-fn", "John")
            val settings = CommandLineArgumentSettings(args)
            settings.firstName shouldBe "John"
        }

        "should correctly parse last name" {
            val args = arrayOf("-ln", "Doe")
            val settings = CommandLineArgumentSettings(args)
            settings.lastName shouldBe "Doe"
        }

        "should correctly parse github username" {
            val args = arrayOf("-gn", "johndoe")
            val settings = CommandLineArgumentSettings(args)
            settings.githubUsername shouldBe "johndoe"
        }

        "should correctly parse provider github" {
            val args = arrayOf("-pg", "github")
            val settings = CommandLineArgumentSettings(args)
            settings.providerGithub shouldBe "github"
        }

        "should correctly parse repo prefix" {
            val args = arrayOf("-rp", "repo")
            val settings = CommandLineArgumentSettings(args)
            settings.repoPrefix shouldBe "repo"
        }

        "should correctly parse repo dir prefix" {
            val args = arrayOf("-dp", "dir")
            val settings = CommandLineArgumentSettings(args)
            settings.repoDirPrefix shouldBe "dir"
        }

        "should correctly parse task" {
            val args = arrayOf("-t", "01")
            val settings = CommandLineArgumentSettings(args)
            settings.task shouldBe "01"
        }

        "should correctly parse base name" {
            val args = arrayOf("-bn", "base")
            val settings = CommandLineArgumentSettings(args)
            settings.baseName shouldBe "base"
        }

        "should correctly parse base suffix" {
            val args = arrayOf("-bs", "suffix")
            val settings = CommandLineArgumentSettings(args)
            settings.baseSuffix shouldBe "suffix"
        }

        "should correctly parse pdf base" {
            val args = arrayOf("-pdf", "pdf")
            val settings = CommandLineArgumentSettings(args)
            settings.pdfBase shouldBe "pdf"
        }


        // You can also add tests for combinations of arguments
        "should correctly parse multiple arguments" {
            val args = arrayOf("--student-id", "12345", "-fn", "John", "-ln", "Doe")
            val settings = CommandLineArgumentSettings(args)
            settings.studentId shouldBe "12345"
            settings.firstName shouldBe "John"
            settings.lastName shouldBe "Doe"
        }

        // Add tests for edge cases, like missing arguments or unexpected values
        "should handle missing arguments gracefully" {
            val args = arrayOf("-fn", "John") // Missing last name
            val settings = CommandLineArgumentSettings(args)
            settings.firstName shouldBe "John"
            settings.lastName shouldBe null // Expecting null since it's not provided
        }

        // Add a test for the help flag (-h)
        "should print help message and exit for help flag" {
            val args = arrayOf("-h")
            // Testing this might require a different approach since it calls exitProcess
        }
    }
})

class PropertiesSettingsTest : FreeSpec({

    "PropertiesSettings" - {
        "should correctly parse properties" {
            val properties = Properties().apply {
                setProperty("STUDENT_ID", "12345")
                setProperty("FIRST_NAME", "John")
                setProperty("LAST_NAME", "Doe")
                setProperty("GITHUB_USERNAME", "johndoe")
                setProperty("PROVIDER_GITHUB", "github")
                setProperty("REPO_PREFIX", "repo")
                setProperty("REPO_DIR_PREFIX", "dir")
                setProperty("TASK", "H01")
                setProperty("BASE_NAME", "base")
                setProperty("BASE_SUFFIX", "suffix")
                setProperty("PDF_BASE", "pdf")
            }

            val settings = PropertiesSettings(properties)

            settings.studentId shouldBe "12345"
            settings.firstName shouldBe "John"
            settings.lastName shouldBe "Doe"
            settings.githubUsername shouldBe "johndoe"
            settings.providerGithub shouldBe "github"
            settings.repoPrefix shouldBe "repo"
            settings.repoDirPrefix shouldBe "dir"
            settings.task shouldBe "H01"
            settings.baseName shouldBe "base"
            settings.baseSuffix shouldBe "suffix"
            settings.pdfBase shouldBe "pdf"
        }

        "should handle null properties object" {
            val settings = PropertiesSettings(null)

            settings.studentId shouldBe null
            settings.firstName shouldBe null
            settings.lastName shouldBe null
            settings.githubUsername shouldBe null
            settings.providerGithub shouldBe null
            settings.repoPrefix shouldBe null
            settings.repoDirPrefix shouldBe null
            settings.task shouldBe null
            settings.baseName shouldBe null
            settings.baseSuffix shouldBe null
            settings.pdfBase shouldBe null
        }

        "should handle missing properties" {
            val properties = Properties()

            val settings = PropertiesSettings(properties)

            settings.studentId shouldBe null
            settings.firstName shouldBe null
            settings.lastName shouldBe null
            settings.githubUsername shouldBe null
            settings.providerGithub shouldBe null
            settings.repoPrefix shouldBe null
            settings.repoDirPrefix shouldBe null
            settings.task shouldBe null
            settings.baseName shouldBe null
            settings.baseSuffix shouldBe null
            settings.pdfBase shouldBe null
        }

    }
})

class SettingsJoinTest : FreeSpec({

    "SettingsJoin" - {
        "should select first over second" {
            val source1 = object : SettingsSource {
                override val debugEnabled: Boolean = false
                override val studentId = "12345"
                override val firstName = "John"
                override val lastName = "Doe"
                override val githubUsername = "johndoe"
                override val providerGithub = "github"
                override val repoPrefix = "repo"
                override val repoDirPrefix = "dir"
                override val task = "H01"
                override val baseName = "base"
                override val baseSuffix = "suffix"
                override val pdfBase = "pdf"
            }

            val source2 = object : SettingsSource {
                override val debugEnabled: Boolean = true
                override val studentId = "54321"
                override val firstName = "Jane"
                override val lastName = "Doe"
                override val githubUsername = "janedoe"
                override val providerGithub = "github"
                override val repoPrefix = "repo"
                override val repoDirPrefix = "dir"
                override val task = "H02"
                override val baseName = "base"
                override val baseSuffix = "suffix"
                override val pdfBase = "pdf"
            }

            val settingsJoin = SettingsJoin(listOf(source1, source2))
            settingsJoin.debugEnabled shouldBe true
            settingsJoin.studentId shouldBe "12345"
            settingsJoin.firstName shouldBe "John"
            settingsJoin.lastName shouldBe "Doe"
            settingsJoin.githubUsername shouldBe "johndoe"
            settingsJoin.providerGithub shouldBe "github"
            settingsJoin.repoPrefix shouldBe "repo"
            settingsJoin.repoDirPrefix shouldBe "dir"
            settingsJoin.task shouldBe "H01"
            settingsJoin.baseName shouldBe "base"
            settingsJoin.baseSuffix shouldBe "suffix"
            settingsJoin.pdfBase shouldBe "pdf"
        }

        "should select second if first is null" {
            val source1 = object : SettingsSource {
                override val debugEnabled: Boolean = false
                override val studentId = null
                override val firstName = null
                override val lastName = null
                override val githubUsername = null
                override val providerGithub = null
                override val repoPrefix = null
                override val repoDirPrefix = null
                override val task = null
                override val baseName = null
                override val baseSuffix = null
                override val pdfBase = null
            }

            val source2 = object : SettingsSource {
                override val debugEnabled: Boolean = true
                override val studentId = "54321"
                override val firstName = "Jane"
                override val lastName = "Doe"
                override val githubUsername = "janedoe"
                override val providerGithub = "github"
                override val repoPrefix = "repo"
                override val repoDirPrefix = "dir"
                override val task = "H02"
                override val baseName = "base"
                override val baseSuffix = "suffix"
                override val pdfBase = "pdf"
            }

            val settingsJoin = SettingsJoin(listOf(source1, source2))

            settingsJoin.debugEnabled shouldBe true
            settingsJoin.studentId shouldBe "54321"
            settingsJoin.firstName shouldBe "Jane"
            settingsJoin.lastName shouldBe "Doe"
            settingsJoin.githubUsername shouldBe "janedoe"
            settingsJoin.providerGithub shouldBe "github"
            settingsJoin.repoPrefix shouldBe "repo"
            settingsJoin.repoDirPrefix shouldBe "dir"
            settingsJoin.task shouldBe "H02"
            settingsJoin.baseName shouldBe "base"
            settingsJoin.baseSuffix shouldBe "suffix"
            settingsJoin.pdfBase shouldBe "pdf"
        }

        "should select first if second is null" {
            val source1 = object : SettingsSource {
                override val debugEnabled: Boolean = false
                override val studentId = "12345"
                override val firstName = "John"
                override val lastName = "Doe"
                override val githubUsername = "johndoe"
                override val providerGithub = "github"
                override val repoPrefix = "repo"
                override val repoDirPrefix = "dir"
                override val task = "H01"
                override val baseName = "base"
                override val baseSuffix = "suffix"
                override val pdfBase = "pdf"
            }

            val source2 = object : SettingsSource {
                override val debugEnabled: Boolean = false
                override val studentId = null
                override val firstName = null
                override val lastName = null
                override val githubUsername = null
                override val providerGithub = null
                override val repoPrefix = null
                override val repoDirPrefix = null
                override val task = null
                override val baseName = null
                override val baseSuffix = null
                override val pdfBase = null
            }

            val settingsJoin = SettingsJoin(listOf(source1, source2))

            settingsJoin.debugEnabled shouldBe false
            settingsJoin.studentId shouldBe "12345"
            settingsJoin.firstName shouldBe "John"
            settingsJoin.lastName shouldBe "Doe"
            settingsJoin.githubUsername shouldBe "johndoe"
            settingsJoin.providerGithub shouldBe "github"
            settingsJoin.repoPrefix shouldBe "repo"
            settingsJoin.repoDirPrefix shouldBe "dir"
            settingsJoin.task shouldBe "H01"
            settingsJoin.baseName shouldBe "base"
            settingsJoin.baseSuffix shouldBe "suffix"
            settingsJoin.pdfBase shouldBe "pdf"
        }



        "should handle empty list of sources" {
            val settingsJoin = SettingsJoin(emptyList())

            settingsJoin.debugEnabled shouldBe false
            settingsJoin.studentId shouldBe null
            settingsJoin.firstName shouldBe null
            settingsJoin.lastName shouldBe null
            settingsJoin.githubUsername shouldBe null
            settingsJoin.providerGithub shouldBe null
            settingsJoin.repoPrefix shouldBe null
            settingsJoin.repoDirPrefix shouldBe null
            settingsJoin.task shouldBe null
            settingsJoin.baseName shouldBe null
            settingsJoin.baseSuffix shouldBe null
            settingsJoin.pdfBase shouldBe null
        }
    }
})
