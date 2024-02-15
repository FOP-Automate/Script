package io.github.fop_automate.api

import java.io.File

val runtime = Runtime.getRuntime()

fun executeCommand(command: String, vararg args: String, cwd: String? = null): String {
    val workingDir = cwd?.let { File(it) } ?: File(".").absoluteFile.parentFile

    println(
        "> ${workingDir.name}  $ $command ${
            args.joinToString(" ") {
                if (it.contains(" ")) "\"$it\"" else it
            }
        }"
    )
    val process = runtime.exec(arrayOf(command, *args), null, cwd?.let { File(it) })
    process.waitFor()
    val output = process.inputStream.bufferedReader().readText()
    val error = process.errorStream.bufferedReader().readText()
    if (process.exitValue() != 0) {
        throw RuntimeException("Command \"$command ${args.joinToString(" ")}\" failed with exit code ${process.exitValue()} and error: $error")
    }

    process.destroy()

    return output
}