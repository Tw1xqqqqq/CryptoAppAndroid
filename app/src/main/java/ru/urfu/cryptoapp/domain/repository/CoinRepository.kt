package ru.urfu.cryptoapp.domain.repository

import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinMarket

/**
 * Источник данных о монетах. Сейчас реализован на моках,
 * в следующих практиках сюда подключится CoinGecko API.
 */
interface CoinRepository {

    /** `/coins/markets` — страница списка (нумерация страниц с 1, как в API). */
    suspend fun getMarkets(page: Int, perPage: Int, vsCurrency: String = "usd"): List<CoinMarket>

    /** `/coins/{id}` — карточка монеты. */
    suspend fun getCoinDetails(id: String): CoinDetails

    /** Рыночные данные одной монеты (для шапки экрана деталей). */
    suspend fun getCoinMarket(id: String, vsCurrency: String = "usd"): CoinMarket?
}
