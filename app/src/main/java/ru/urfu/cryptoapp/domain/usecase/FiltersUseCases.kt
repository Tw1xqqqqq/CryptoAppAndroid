package ru.urfu.cryptoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.repository.FiltersRepository

class ObserveFiltersUseCase(private val repository: FiltersRepository) {
    operator fun invoke(): Flow<CoinFilters> = repository.filters.distinctUntilChanged()
}

class SaveFiltersUseCase(private val repository: FiltersRepository) {
    suspend operator fun invoke(filters: CoinFilters) = repository.save(filters)
}
