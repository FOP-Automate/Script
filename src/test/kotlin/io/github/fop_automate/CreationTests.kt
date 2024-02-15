package io.github.fop_automate

import io.github.fop_automate.api.WORKING_DIR
import io.github.fop_automate.api.executeCommand
import io.github.fop_automate.api.initSettingsProperties
import io.github.fop_automate.create.main
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempdir
import java.util.*

class CreationTests : FreeSpec({

    val prefix = (0..7).joinToString("") { ('a'..'z').random().toString() }

    // Test with all 2223 and 2324 repositories
    "2324" - {
        for (i in 0..13) {
            val h = if (i < 10) "H0$i" else "H$i"
            "should create a repository for $h" {

                val workingDir = tempdir()

                val settings = """
                    STUDENT_ID=hp42pfui
                    FIRST_NAME=Harry
                    LAST_NAME=Potter
                    GITHUB_USERNAME=FOP-Automate
                    PROVIDER_GITHUB=FOP-2324
                    REPO_PREFIX=temp-repo-$prefix-
                    REPO_DIR_PREFIX=temp-repo-$prefix-
                    BASE_NAME=FOP-2324-
                    BASE_SUFFIX=-Student
                    PDF_BASE=https://raw.githubusercontent.com/FOP-2324/FOP-2324-TeX/master/
                  """.trimIndent()

                val properties = Properties()
                properties.load(settings.byteInputStream())
                initSettingsProperties(properties)

                WORKING_DIR = workingDir

                main(arrayOf("-t", h))

                // Cleanup
                executeCommand("gh", "repo", "delete", "--yes", "FOP-Automate/temp-repo-$prefix-FOP-2324-$h-Student")
            }
        }
    }

    "2223" - {
        for (i in 0..13) {
            val h = if (i < 10) "H0$i" else "H$i"
            "should create a repository for $h" {

                val workingDir = tempdir()

                val settings = """
                    STUDENT_ID=hp42pfui
                    FIRST_NAME=Harry
                    LAST_NAME=Potter
                    GITHUB_USERNAME=FOP-Automate
                    PROVIDER_GITHUB=FOP-2223
                    REPO_PREFIX=temp-repo-$prefix-
                    REPO_DIR_PREFIX=temp-repo-$prefix-
                    BASE_NAME=FOP-2223-
                    BASE_SUFFIX=-Student
                    PDF_BASE=https://raw.githubusercontent.com/FOP-2223/FOP-2223-TeX/master/
                  """.trimIndent()

                val properties = Properties()
                properties.load(settings.byteInputStream())
                initSettingsProperties(properties)

                WORKING_DIR = workingDir

                main(arrayOf("-t", h))

                // Cleanup
                executeCommand("gh", "repo", "delete", "--yes", "FOP-Automate/temp-repo-$prefix-FOP-2223-$h-Student")
            }
        }
    }

})