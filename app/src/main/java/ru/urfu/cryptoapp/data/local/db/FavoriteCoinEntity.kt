package ru.urfu.cryptoapp.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Избранная монета: снимок рыночных данных на момент добавления. */
@Entity(tableName = "favorite_coins")
data class FavoriteCoinEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: Double,
    val marketCap: Double,
    val marketCapRank: Int,
    val totalVolume: Double,
    val high24h: Double,
    val low24h: Double,
    val priceChangePercentage24h: Double,
    val circulatingSupply: Double,
    val maxSupply: Double?,
    val currency: String,
    val addedAt: Long,
)
