package org.signal.core.util

import java.util.Locale

fun Double.roundedString(places: Int): String {
    return String.format(Locale.US, "%.${places}f", this)
}