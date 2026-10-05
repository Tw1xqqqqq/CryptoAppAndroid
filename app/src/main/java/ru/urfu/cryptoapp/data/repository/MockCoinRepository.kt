package ru.urfu.cryptoapp.data.repository

import kotlinx.coroutines.delay
import ru.urfu.cryptoapp.data.mock.MockCoinData
import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.repository.CoinRepository

/** Репозиторий на моках: имитирует задержку сети и постраничную выдачу. */
class MockCoinRepository : CoinRepository {

    override suspend fun getMarkets(page: Int, perPage: Int, vsCurrency: String): List<CoinMarket> {
        delay(NETWORK_DELAY_MS)
        val from = (page - 1) * perPage
        if (from >= MockCoinData.markets.size) return emptyList()
        val to = minOf(from + perPage, MockCoinData.markets.size)
        return MockCoinData.markets.subList(from, to)
    }

    override suspend fun getCoinDetails(id: String): CoinDetails {
        delay(NETWORK_DELAY_MS)
        return MockCoinData.details(id) ?: throw NoSuchElementException("Coin '$id' not found")
    }

    override suspend fun getCoinMarket(id: String, vsCurrency: String): CoinMarket? =
        MockCoinData.markets.firstOrNull { it.id == id }

    private companion object {
        const val NETWORK_DELAY_MS = 700L
    }
}
