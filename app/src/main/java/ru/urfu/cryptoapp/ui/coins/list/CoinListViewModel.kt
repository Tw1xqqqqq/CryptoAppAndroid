package ru.urfu.cryptoapp.ui.coins.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.di.AppContainer
import ru.urfu.cryptoapp.domain.repository.CoinRepository
import ru.urfu.cryptoapp.ui.coins.model.CoinListItemUi
import ru.urfu.cryptoapp.ui.coins.model.CoinUiMapper

data class CoinListUiState(
    val coins: List<CoinListItemUi> = emptyList(),
    val isLoading: Boolean = false,      // первая страница
    val isLoadingMore: Boolean = false,  // следующие страницы
    val endReached: Boolean = false,
    val error: String? = null,
)

class CoinListViewModel(
    private val repository: CoinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinListUiState())
    val uiState: StateFlow<CoinListUiState> = _uiState.asStateFlow()

    private var nextPage = 1

    init {
        loadNextPage()
    }

    /** Подгрузка следующей страницы (per_page/page как в /coins/markets). */
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.endReached) return

        val isFirstPage = nextPage == 1
        _uiState.update { it.copy(isLoading = isFirstPage, isLoadingMore = !isFirstPage, error = null) }

        viewModelScope.launch {
            runCatching { repository.getMarkets(page = nextPage, perPage = PAGE_SIZE) }
                .onSuccess { page ->
                    nextPage++
                    _uiState.update {
                        it.copy(
                            coins = it.coins + page.map(CoinUiMapper::toListItem),
                            isLoading = false,
                            isLoadingMore = false,
                            endReached = page.size < PAGE_SIZE,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isLoadingMore = false, error = e.message.orEmpty())
                    }
                }
        }
    }

    companion object {
        const val PAGE_SIZE = 10

        val Factory = viewModelFactory {
            initializer { CoinListViewModel(AppContainer.coinRepository) }
        }
    }
}
