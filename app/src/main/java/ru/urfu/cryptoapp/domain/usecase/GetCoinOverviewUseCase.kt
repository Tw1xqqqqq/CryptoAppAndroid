package ru.urfu.cryptoapp.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.urfu.cryptoapp.domain.model.CoinOverview
import ru.urfu.cryptoapp.domain.repository.CoinRepository
import ru.urfu.cryptoapp.domain.repository.FiltersRepository

/**
 * Данные для экрана деталей. Оба запроса выполняются параллельно (combine).
 * Карточка монеты обязательна, а рыночные данные — нет: если они не загрузились,
 * экран всё равно показывается, просто без цены.
 * Цены — в валюте, выбранной в фильтрах.
 */
class GetCoinOverviewUseCase(
    private val coinRepository: CoinRepository,
    private val filtersRepository: FiltersRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(coinId: String): Flow<CoinOverview> = filtersRepository.filters
        .map { it.currency }
        .distinctUntilChanged()
        .flatMapLatest { currency ->
            combine(
                coinRepository.observeCoinDetails(coinId),
                coinRepository.observeCoinMarket(coinId, currency).catch { emit(null) },
            ) { details, market -> CoinOverview(details, market) }
        }
}
