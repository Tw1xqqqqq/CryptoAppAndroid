package ru.urfu.cryptoapp.ui.coins.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.CryptoApplication
import ru.urfu.cryptoapp.domain.model.DataError
import ru.urfu.cryptoapp.domain.model.toDataError
import ru.urfu.cryptoapp.domain.usecase.GetCoinMarketsUseCase
import ru.urfu.cryptoapp.ui.coins.model.CoinListItemUi
import ru.urfu.cryptoapp.ui.coins.model.CoinUiMapper

data class CoinListUiState(
    val coins: List<CoinListItemUi> = emptyList(),
    val isLoading: Boolean = false,      // первая страница
    val isLoadingMore: Boolean = false,  // следующие страницы
    val isRefreshing: Boolean = false,   // pull-to-refresh
    val endReached: Boolean = false,
    val error: DataError? = null,        // ошибка загрузки страницы
    val refreshError: DataError? = null, // ошибка обновления — показывается снекбаром поверх старых данных
)

class CoinListViewModel(
    private val getCoinMarkets: GetCoinMarketsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinListUiState())
    val uiState: StateFlow<CoinListUiState> = _uiState.asStateFlow()

    private var nextPage = 1
    private var loadJob: Job? = null

    init {
        loadNextPage()
    }

    /** Подгрузка следующей страницы (`page` / `per_page` в запросе `/coins/markets`). */
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isRefreshing || state.endReached) return

        val page = nextPage
        val isFirstPage = page == 1
        _uiState.update { it.copy(isLoading = isFirstPage, isLoadingMore = !isFirstPage, error = null) }

        loadJob = viewModelScope.launch {
            try {
                val coins = getCoinMarkets(page = page, perPage = PAGE_SIZE)
                nextPage = page + 1
                _uiState.update {
                    it.copy(
                        coins = it.coins + coins.map(CoinUiMapper::toListItem),
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
        if (_uiState.value.isRefreshing) return
        loadJob?.cancel()
        _uiState.update {
            it.copy(isRefreshing = true, isLoading = false, isLoadingMore = false, refreshError = null)
        }

        loadJob = viewModelScope.launch {
            try {
                val coins = getCoinMarkets(page = 1, perPage = PAGE_SIZE, forceRefresh = true)
                nextPage = 2
                _uiState.value = CoinListUiState(
                    coins = coins.map(CoinUiMapper::toListItem),
                    endReached = coins.size < PAGE_SIZE,
                )
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

    companion object {
        const val PAGE_SIZE = 20

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CryptoApplication
                CoinListViewModel(app.container.getCoinMarketsUseCase)
            }
        }
    }
}
