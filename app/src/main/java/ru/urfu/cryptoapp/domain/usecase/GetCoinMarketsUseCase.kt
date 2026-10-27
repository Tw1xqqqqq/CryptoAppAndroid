package ru.urfu.cryptoapp.domain.usecase

import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.repository.CoinRepository

/** Страница списка монет с учётом фильтров. */
class GetCoinMarketsUseCase(private val repository: CoinRepository) {

    suspend operator fun invoke(
        page: Int,
        perPage: Int,
        filters: CoinFilters,
        forceRefresh: Boolean = false,
    ): List<CoinMarket> {
        require(page >= 1) { "page starts from 1" }
        require(perPage in 1..MAX_PER_PAGE) { "per_page must be in 1..$MAX_PER_PAGE" }
        return repository.getMarkets(page, perPage, filters, forceRefresh)
    }

    private companion object {
        const val MAX_PER_PAGE = 250 // ограничение CoinGecko
    }
}
