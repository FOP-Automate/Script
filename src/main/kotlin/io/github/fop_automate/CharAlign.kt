package io.github.fop_automate

fun String.alignLeft(width: Int): String {
    return this.padEnd(width - this.length)
}

fun String.alignRight(width: Int): String {
    return this.padStart(width - this.length)
}

fun String.alignCenter( width: Int): String {
    val pad = width - this.length
    val padLeft = pad / 2 + this.length
    return this.padStart(padLeft).padEnd(width)
}
