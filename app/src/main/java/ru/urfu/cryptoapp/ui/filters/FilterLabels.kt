package ru.urfu.cryptoapp.ui.filters

import androidx.annotation.StringRes
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.model.CoinCategory
import ru.urfu.cryptoapp.domain.model.SortOrder
import ru.urfu.cryptoapp.domain.model.VsCurrency

@get:StringRes
val VsCurrency.labelRes: Int
    get() = when (this) {
        VsCurrency.USD -> R.string.currency_usd
        VsCurrency.EUR -> R.string.currency_eur
        VsCurrency.RUB -> R.string.currency_rub
    }

@get:StringRes
val SortOrder.labelRes: Int
    get() = when (this) {
        SortOrder.MARKET_CAP_DESC -> R.string.order_market_cap_desc
        SortOrder.MARKET_CAP_ASC -> R.string.order_market_cap_asc
        SortOrder.VOLUME_DESC -> R.string.order_volume_desc
        SortOrder.VOLUME_ASC -> R.string.order_volume_asc
    }

@get:StringRes
val CoinCategory.labelRes: Int
    get() = when (this) {
        CoinCategory.ALL -> R.string.category_all
        CoinCategory.LAYER_1 -> R.string.category_layer_1
        CoinCategory.SMART_CONTRACT -> R.string.category_smart_contract
        CoinCategory.DEFI -> R.string.category_defi
        CoinCategory.MEME -> R.string.category_meme
        CoinCategory.STABLECOINS -> R.string.category_stablecoins
        CoinCategory.AI -> R.string.category_ai
        CoinCategory.GAMING -> R.string.category_gaming
    }
