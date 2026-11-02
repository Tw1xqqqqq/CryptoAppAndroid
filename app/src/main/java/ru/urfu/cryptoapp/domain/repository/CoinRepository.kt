package ru.urfu.cryptoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinMarket

/**
 * Источник данных о монетах (CoinGecko API + кэш в памяти).
 * Все методы main-safe и при ошибке бросают [ru.urfu.cryptoapp.domain.model.DataException].
 */
interface CoinRepository {

    /**
     * `/coins/markets` — страница списка (нумерация страниц с 1, как в API).
     * [forceRefresh] — игнорировать кэш и сходить в сеть.
     */
    suspend fun getMarkets(
        page: Int,
        perPage: Int,
        vsCurrency: String = DEFAULT_CURRENCY,
        forceRefresh: Boolean = false,
    ): List<CoinMarket>

    /** `/coins/{id}` — карточка монеты. Сначала отдаёт кэш (если есть), затем свежие данные из сети. */
    fun observeCoinDetails(id: String): Flow<CoinDetails>

    /** Рыночные данные одной монеты (`/coins/markets?ids={id}`); null — монета не торгуется. */
    fun observeCoinMarket(id: String, vsCurrency: String = DEFAULT_CURRENCY): Flow<CoinMarket?>

    companion object {
        const val DEFAULT_CURRENCY = "usd"
    }
}
