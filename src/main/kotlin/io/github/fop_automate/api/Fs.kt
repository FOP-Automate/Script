package io.github.fop_automate.api

import java.io.File

var WORKING_DIR = File(".").absoluteFile.parentFile

fun file (path: String) = WORKING_DIR.resolve(path)