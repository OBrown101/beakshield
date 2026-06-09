package com.beakshield

import kotlin.math.round

fun Int.formatWithSuffix(): String = when {
    this >= 1_000_000 -> {
         val v = round(this / 1_000_000.0 * 10) / 10.0
         "${v}${if (v % 1.0 == 0.0) "M" else "M"}"
    }
    this >= 1_000 -> {
         val v = round(this / 1_000.0 * 10) / 10.0
         "${v}${if (v % 1.0 == 0.0) "K" else "K"}"
    }
    else -> this.toString()
}

fun String.capitalizeString(): String = this.replaceFirstChar { it.uppercaseChar() }