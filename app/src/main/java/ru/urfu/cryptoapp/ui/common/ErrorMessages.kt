package ru.urfu.cryptoapp.ui.common

import androidx.annotation.StringRes
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.model.DataError

@StringRes
fun DataError.messageRes(): Int = when (this) {
    DataError.NETWORK -> R.string.error_network
    DataError.RATE_LIMIT -> R.string.error_rate_limit
    DataError.NOT_FOUND -> R.string.error_not_found
    DataError.SERVER -> R.string.error_server
    DataError.UNKNOWN -> R.string.error_generic
}
