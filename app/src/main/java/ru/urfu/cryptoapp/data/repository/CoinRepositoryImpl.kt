package ru.urfu.cryptoapp.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.urfu.cryptoapp.data.cache.MemoryCache
import ru.urfu.cryptoapp.data.mapper.toDomain
import ru.urfu.cryptoapp.data.remote.CoinGeckoApi
import ru.urfu.cryptoapp.data.remote.apiValue
import ru.urfu.cryptoapp.data.remote.safeApiCall
import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.model.DataException
import ru.urfu.cryptoapp.domain.model.VsCurrency
import ru.urfu.cryptoapp.domain.repository.CoinRepository

/**
 * Репозиторий поверх CoinGecko API.
 * Запросы и маппинг выполняются на [ioDispatcher]; ответы кэшируются в памяти,
 * чтобы не упираться в лимит запросов демо-режима и мгновенно открывать уже просмотренные монеты.
 */
class CoinRepositoryImpl(
    private val api: CoinGeckoApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CoinRepository {

    private data class PageKey(val filters: CoinFilters, val page: Int, val perPage: Int)
    private data class MarketKey(val currency: VsCurrency, val id: String)

    private val marketPages = MemoryCache<PageKey, List<CoinMarket>>(MARKETS_TTL_MS)
    private val markets = MemoryCache<MarketKey, CoinMarket>(MARKETS_TTL_MS)
    private val details = MemoryCache<String, CoinDetails>(DETAILS_TTL_MS)

    override suspend fun getMarkets(
        page: Int,
        perPage: Int,
        filters: CoinFilters,
        forceRefresh: Boolean,
    ): List<CoinMarket> = withContext(ioDispatcher) {
        val key = PageKey(filters, page, perPage)
        val cached = marketPages[key]
        if (cached != null && cached.isFresh && !forceRefresh) return@withContext cached.value

        val coins = try {
            safeApiCall {
                api.getMarkets(
                    vsCurrency = filters.currency.apiValue,
                    perPage = perPage,
                    page = page,
                    order = filters.order.apiValue,
                    category = filters.category.apiValue,
                )
            }.map { it.toDomain(filters.currency) }
        } catch (e: DataException) {
            // Сеть недоступна, но страница уже загружалась — отдаём устаревшие данные
            if (cached != null && !forceRefresh) return@withContext cached.value
            throw e
        }
        marketPages[key] = coins
        coins.forEach { markets[MarketKey(filters.currency, it.id)] = it }
        coins
    }

    override fun observeCoinDetails(id: String): Flow<CoinDetails> =
        cacheThenNetwork(details, id) {
            api.getCoinDetails(id).toDomain()
        }.filterNotNull()

    override fun observeCoinMarket(id: String, currency: VsCurrency): Flow<CoinMarket?> =
        // Обычно монета уже есть в кэше после загрузки списка, и запрос не нужен
        cacheThenNetwork(markets, MarketKey(currency, id)) {
            api.getMarkets(vsCurrency = currency.apiValue, perPage = 1, page = 1, ids = id)
                .firstOrNull()
                ?.toDomain(currency)
        }

    /**
     * Стратегия «сначала кэш, потом сеть»:
     * 1. есть запись в кэше — сразу отдаём её; если она свежая, на этом всё;
     * 2. иначе идём в сеть и отдаём результат;
     * 3. если сеть упала, а что-то из кэша уже показано — остаёмся на нём,
     *    если же показать нечего — пробрасываем ошибку.
     */
    private fun <K : Any, V : Any> cacheThenNetwork(
        cache: MemoryCache<K, V>,
        key: K,
        fetch: suspend () -> V?,
    ): Flow<V?> = flow {
        val cached = cache[key]
        if (cached != null) {
            emit(cached.value)
            if (cached.isFresh) return@flow
        }
        val fresh = try {
            safeApiCall { fetch() }
        } catch (e: DataException) {
            if (cached != null) return@flow
            throw e
        }
        if (fresh != null) cache[key] = fresh
        emit(fresh)
    }.flowOn(ioDispatcher)

    private companion object {
        const val MARKETS_TTL_MS = 60_000L      // цены меняются часто
        const val DETAILS_TTL_MS = 10 * 60_000L // описание и ссылки — редко
    }
}
