package ru.urfu.cryptoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import ru.urfu.cryptoapp.domain.model.CoinOverview
import ru.urfu.cryptoapp.domain.repository.CoinRepository

/**
 * Данные для экрана деталей. Оба запроса выполняются параллельно (combine).
 * Карточка монеты обязательна, а рыночные данные — нет: если они не загрузились,
 * экран всё равно показывается, просто без цены.
 */
class GetCoinOverviewUseCase(private val repository: CoinRepository) {

    operator fun invoke(
        coinId: String,
        vsCurrency: String = CoinRepository.DEFAULT_CURRENCY,
    ): Flow<CoinOverview> = combine(
        repository.observeCoinDetails(coinId),
        repository.observeCoinMarket(coinId, vsCurrency).catch { emit(null) },
    ) { details, market -> CoinOverview(details, market) }
}
