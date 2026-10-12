package ru.urfu.cryptoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Элемент ответа `GET /coins/markets`. У малоизвестных монет многие поля приходят null. */
@Serializable
data class CoinMarketDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String? = null,
    @SerialName("current_price") val currentPrice: Double? = null,
    @SerialName("market_cap") val marketCap: Double? = null,
    @SerialName("market_cap_rank") val marketCapRank: Int? = null,
    @SerialName("total_volume") val totalVolume: Double? = null,
    @SerialName("high_24h") val high24h: Double? = null,
    @SerialName("low_24h") val low24h: Double? = null,
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @SerialName("circulating_supply") val circulatingSupply: Double? = null,
    @SerialName("max_supply") val maxSupply: Double? = null,
)
