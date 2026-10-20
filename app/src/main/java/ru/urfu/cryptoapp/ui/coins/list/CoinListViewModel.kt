package ru.urfu.cryptoapp.ui.coins.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.model.DataError
import ru.urfu.cryptoapp.domain.model.toDataError
import ru.urfu.cryptoapp.domain.usecase.GetCoinMarketsUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveFiltersUseCase
import ru.urfu.cryptoapp.ui.filters.FilterBadgeCache

data class CoinListUiState(
    val coins: List<CoinMarket> = emptyList(),
    val isLoading: Boolean = true,       // первая страница (true, пока не прочитаны фильтры)
    val isLoadingMore: Boolean = false,  // следующие страницы
    val isRefreshing: Boolean = false,   // pull-to-refresh
    val endReached: Boolean = false,
    val error: DataError? = null,        // ошибка загрузки страницы
    val refreshError: DataError? = null, // ошибка обновления — показывается снекбаром поверх старых данных
    val showFilterBadge: Boolean = false,
)

class CoinListViewModel(
    private val getCoinMarkets: GetCoinMarketsUseCase,
    observeFilters: ObserveFiltersUseCase,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinListUiState())
    val uiState: StateFlow<CoinListUiState> = _uiState.asStateFlow()

    private var filters: CoinFilters? = null
    private var nextPage = 1
    private var loadJob: Job? = null

    init {
        // Сохранённые фильтры: при запуске и после «Готово» на экране фильтров список грузится заново
        observeFilters()
            .onEach { newFilters ->
                filters = newFilters
                badgeCache.update(newFilters)
                reload()
            }
            .launchIn(viewModelScope)

        badgeCache.showBadge
            .onEach { show -> _uiState.update { it.copy(showFilterBadge = show) } }
            .launchIn(viewModelScope)
    }

    /** Подгрузка следующей страницы (`page` / `per_page` в запросе `/coins/markets`). */
    fun loadNextPage() {
        val filters = filters ?: return
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isRefreshing || state.endReached) return

        val page = nextPage
        val isFirstPage = page == 1
        _uiState.update { it.copy(isLoading = isFirstPage, isLoadingMore = !isFirstPage, error = null) }

        loadJob = viewModelScope.launch {
            try {
                val coins = getCoinMarkets(page = page, perPage = PAGE_SIZE, filters = filters)
                nextPage = page + 1
                _uiState.update {
                    it.copy(
                        coins = it.coins + coins,
                        isLoading = false,
                        isLoadingMore = false,
                        endReached = coins.size < PAGE_SIZE,
                    )
                }
            } catch (e: Exception) {
                val error = e.toDataError()
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = error) }
            }
        }
    }

    /** Перезагрузка списка с первой страницы в обход кэша. */
    fun refresh() {
        val filters = filters ?: return
        if (_uiState.value.isRefreshing) return
        loadJob?.cancel()
        _uiState.update {
            it.copy(isRefreshing = true, isLoading = false, isLoadingMore = false, refreshError = null)
        }

        loadJob = viewModelScope.launch {
            try {
                val coins = getCoinMarkets(page = 1, perPage = PAGE_SIZE, filters = filters, forceRefresh = true)
                nextPage = 2
                _uiState.update {
                    CoinListUiState(
                        coins = coins,
                        isLoading = false,
                        endReached = coins.size < PAGE_SIZE,
                        showFilterBadge = it.showFilterBadge,
                    )
                }
            } catch (e: Exception) {
                val error = e.toDataError()
                _uiState.update {
                    if (it.coins.isEmpty()) {
                        it.copy(isRefreshing = false, error = error)
                    } else {
                        it.copy(isRefreshing = false, refreshError = error)
                    }
                }
            }
        }
    }

    fun onRefreshErrorShown() {
        _uiState.update { it.copy(refreshError = null) }
    }

    /** Сброс списка и загрузка первой страницы с текущими фильтрами. */
    private fun reload() {
        loadJob?.cancel()
        nextPage = 1
        _uiState.update { CoinListUiState(isLoading = false, showFilterBadge = it.showFilterBadge) }
        loadNextPage()
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
