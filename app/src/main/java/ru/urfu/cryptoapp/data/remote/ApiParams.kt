package ru.urfu.cryptoapp.data.remote

import ru.urfu.cryptoapp.domain.model.CoinCategory
import ru.urfu.cryptoapp.domain.model.SortOrder
import ru.urfu.cryptoapp.domain.model.VsCurrency

/** Значения доменных фильтров в том виде, в каком их ждёт CoinGecko. */

val VsCurrency.apiValue: String
    get() = name.lowercase()

val SortOrder.apiValue: String
    get() = name.lowercase() // market_cap_desc, volume_asc, ...

/** id категорий — из `GET /coins/categories/list`. */
val CoinCategory.apiValue: String?
    get() = when (this) {
        CoinCategory.ALL -> null
        CoinCategory.LAYER_1 -> "layer-1"
        CoinCategory.SMART_CONTRACT -> "smart-contract-platform"
        CoinCategory.DEFI -> "decentralized-finance-defi"
        CoinCategory.MEME -> "meme-token"
        CoinCategory.STABLECOINS -> "stablecoins"
        CoinCategory.AI -> "artificial-intelligence"
        CoinCategory.GAMING -> "gaming"
    }
