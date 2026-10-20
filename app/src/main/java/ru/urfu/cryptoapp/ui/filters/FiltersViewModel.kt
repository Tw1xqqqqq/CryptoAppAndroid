package ru.urfu.cryptoapp.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.domain.model.CoinCategory
import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.model.SortOrder
import ru.urfu.cryptoapp.domain.model.VsCurrency
import ru.urfu.cryptoapp.domain.usecase.ObserveFiltersUseCase
import ru.urfu.cryptoapp.domain.usecase.SaveFiltersUseCase

data class FiltersUiState(
    val filters: CoinFilters = CoinFilters.DEFAULT, // черновик, сохраняется только по «Готово»
    val isLoaded: Boolean = false,
    val isApplied: Boolean = false,                 // сохранено — экран можно закрывать
)

class FiltersViewModel(
    observeFilters: ObserveFiltersUseCase,
    private val saveFilters: SaveFiltersUseCase,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiltersUiState())
    val uiState: StateFlow<FiltersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = observeFilters().first()
            _uiState.update { it.copy(filters = saved, isLoaded = true) }
        }
    }

    fun onCurrencySelected(currency: VsCurrency) = edit { it.copy(currency = currency) }

    fun onOrderSelected(order: SortOrder) = edit { it.copy(order = order) }

    fun onCategorySelected(category: CoinCategory) = edit { it.copy(category = category) }

    fun reset() = edit { CoinFilters.DEFAULT }

    fun apply() {
        val filters = _uiState.value.filters
        viewModelScope.launch {
            saveFilters(filters)
            badgeCache.update(filters)
            _uiState.update { it.copy(isApplied = true) }
        }
    }

    private fun edit(transform: (CoinFilters) -> CoinFilters) {
        _uiState.update { it.copy(filters = transform(it.filters)) }
    }
}
