package ru.urfu.cryptoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.domain.model.CoinFilters

/** Сохранённые настройки списка монет. */
interface FiltersRepository {

    /** Текущие настройки; новое значение приходит после каждого [save]. */
    val filters: Flow<CoinFilters>

    suspend fun save(filters: CoinFilters)
}
