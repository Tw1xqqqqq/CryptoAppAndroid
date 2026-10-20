package ru.urfu.cryptoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.model.VsCurrency

/**
 * Источник данных о монетах (CoinGecko API + кэш в памяти).
 * Все методы main-safe и при ошибке бросают [ru.urfu.cryptoapp.domain.model.DataException].
 */
interface CoinRepository {

    /**
     * `/coins/markets` — страница списка (нумерация страниц с 1, как в API) с учётом [filters].
     * [forceRefresh] — игнорировать кэш и сходить в сеть.
     */
    suspend fun getMarkets(
        page: Int,
        perPage: Int,
        filters: CoinFilters,
        forceRefresh: Boolean = false,
    ): List<CoinMarket>

    /** `/coins/{id}` — карточка монеты. Сначала отдаёт кэш (если есть), затем свежие данные из сети. */
    fun observeCoinDetails(id: String): Flow<CoinDetails>

    /** Рыночные данные одной монеты (`/coins/markets?ids={id}`); null — монета не торгуется. */
    fun observeCoinMarket(id: String, currency: VsCurrency): Flow<CoinMarket?>
}
