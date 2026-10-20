package ru.urfu.cryptoapp.ui.common

import ru.urfu.cryptoapp.domain.model.VsCurrency
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

val VsCurrency.symbol: String
    get() = when (this) {
        VsCurrency.USD -> "$"
        VsCurrency.EUR -> "€"
        VsCurrency.RUB -> "₽"
    }

/** $62,350.00 / €0.5321 / ₽0.00001742 */
fun Double.formatPrice(currency: VsCurrency): String {
    val format = NumberFormat.getNumberInstance(Locale.US)
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = when {
        abs(this) >= 1.0 -> 2
        abs(this) >= 0.01 -> 4
        else -> 8
    }
    val sign = if (this < 0) "-" else ""
    return sign + currency.symbol + format.format(abs(this))
}

/** 1.23T / 456.70B / 12.30M */
fun Double.formatCompact(): String {
    val value = abs(this)
    val sign = if (this < 0) "-" else ""
    return sign + when {
        value >= 1e12 -> "%.2fT".format(Locale.US, value / 1e12)
        value >= 1e9 -> "%.2fB".format(Locale.US, value / 1e9)
        value >= 1e6 -> "%.2fM".format(Locale.US, value / 1e6)
        value >= 1e3 -> "%.2fK".format(Locale.US, value / 1e3)
        else -> "%.2f".format(Locale.US, value)
    }
}

fun Double.formatCompactMoney(currency: VsCurrency): String = currency.symbol + formatCompact()

/** +1.84% / -0.92% */
fun Double.formatPercent(): String = "%+.2f%%".format(Locale.US, this)
