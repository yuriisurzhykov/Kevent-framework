@file:Suppress("unused")

package com.github.yuriisurzhykov.kevent.ksp.core

fun String.camelCase(): String {
    val pattern = "_[a-z]".toRegex()
    return replace(pattern) { it.value.last().uppercase() }.replaceFirstChar { it.lowercase() }
}

fun String.snakeCase(): String {
    val pattern = "(?<=.)[A-Z]".toRegex()
    return this.replace(pattern, "_$0").lowercase()
}

fun String.trimLiterals(applyFilter: Boolean): String {
    val pattern = "^(uL|L|f|u|F|\"+)|(uL|L|f|u|F|\"+)$".toRegex()
    return if (applyFilter) replace(pattern, "") else this
}