package io.github.fop_automate

import java.io.File

val runtime = Runtime.getRuntime()
fun executeCommand(command: String, vararg args: String, cwd: String? = null): String {
    println("> $command ${args.joinToString(" ")}")
    val process = runtime.exec(arrayOf(command, *args), null, cwd?.let { File(it) })
    process.waitFor()
    val output = process.inputStream.bufferedReader().readText()
    val error = process.errorStream.bufferedReader().readText()
    if(process.exitValue() != 0) {
        throw RuntimeException("Command \"$command ${args.joinToString(" ")}\" failed with exit code ${process.exitValue()} and error: $error")
    }

    process.destroy()

    return output
}