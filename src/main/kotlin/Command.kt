package org.example

val runtime = Runtime.getRuntime()
fun executeCommand(command: String): String {
    val process = runtime.exec(command)
    process.waitFor()
    if(process.exitValue() != 0) {
        throw RuntimeException("Command $command failed with exit code ${process.exitValue()}")
    }

    val output = process.inputStream.bufferedReader().readText()
    val error = process.errorStream.bufferedReader().readText()
    if(error.isNotEmpty()) {
        throw RuntimeException("Command $command failed with error: $error")
    }

    process.destroy()

    return output
}