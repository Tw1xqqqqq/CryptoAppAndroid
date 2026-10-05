package ru.urfu.cryptoapp.domain.model

/**
 * Элемент списка монет.
 * Соответствует ответу `GET /coins/markets?vs_currency=usd&order=market_cap_desc&per_page=..&page=..`
 */
data class CoinMarket(
    val id: String,                         // id
    val symbol: String,                     // symbol
    val name: String,                       // name
    val imageUrl: String,                   // image
    val currentPrice: Double,               // current_price
    val marketCap: Double,                  // market_cap
    val marketCapRank: Int,                 // market_cap_rank
    val totalVolume: Double,                // total_volume
    val high24h: Double,                    // high_24h
    val low24h: Double,                     // low_24h
    val priceChangePercentage24h: Double,   // price_change_percentage_24h
    val circulatingSupply: Double,          // circulating_supply
    val maxSupply: Double?,                 // max_supply (null — эмиссия не ограничена)
)
