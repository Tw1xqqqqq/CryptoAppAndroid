package ru.urfu.cryptoapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.urfu.cryptoapp.data.local.db.FavoriteCoinEntity
import ru.urfu.cryptoapp.data.local.db.FavoritesDao
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.model.VsCurrency
import ru.urfu.cryptoapp.domain.repository.FavoritesRepository

/** Избранное в Room. Запросы Room сами выполняются в фоновом потоке, а Flow обновляется при изменении таблицы. */
class FavoritesRepositoryImpl(
    private val dao: FavoritesDao,
    private val now: () -> Long = System::currentTimeMillis,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<CoinMarket>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeIsFavorite(id: String): Flow<Boolean> = dao.observeExists(id)

    override suspend fun add(coin: CoinMarket) = dao.upsert(coin.toEntity(addedAt = now()))

    override suspend fun remove(id: String) = dao.delete(id)
}

private fun FavoriteCoinEntity.toDomain() = CoinMarket(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    totalVolume = totalVolume,
    high24h = high24h,
    low24h = low24h,
    priceChangePercentage24h = priceChangePercentage24h,
    circulatingSupply = circulatingSupply,
    maxSupply = maxSupply,
    currency = VsCurrency.entries.firstOrNull { it.name == currency } ?: VsCurrency.USD,
)

private fun CoinMarket.toEntity(addedAt: Long) = FavoriteCoinEntity(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    totalVolume = totalVolume,
    high24h = high24h,
    low24h = low24h,
    priceChangePercentage24h = priceChangePercentage24h,
    circulatingSupply = circulatingSupply,
    maxSupply = maxSupply,
    currency = currency.name,
    addedAt = addedAt,
)
