package ru.urfu.cryptoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.domain.model.CoinMarket

/** Избранные монеты. Хранятся локально и доступны без интернета. */
interface FavoritesRepository {

    /** Избранное, последние добавленные — первыми. */
    fun observeFavorites(): Flow<List<CoinMarket>>

    fun observeIsFavorite(id: String): Flow<Boolean>

    /** Сохраняет снимок рыночных данных монеты на момент добавления. */
    suspend fun add(coin: CoinMarket)

    suspend fun remove(id: String)
}
