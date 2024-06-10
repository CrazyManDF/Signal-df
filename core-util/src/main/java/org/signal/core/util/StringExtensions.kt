package org.signal.core.util


fun String.toSingleLine(): String {
    return this.trimIndent().split("\n").joinToString(separator = " ")
}