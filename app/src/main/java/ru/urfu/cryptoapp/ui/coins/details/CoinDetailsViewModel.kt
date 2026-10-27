package ru.urfu.cryptoapp.ui.coins.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.model.DataError
import ru.urfu.cryptoapp.domain.model.toDataError
import ru.urfu.cryptoapp.domain.usecase.AddFavoriteUseCase
import ru.urfu.cryptoapp.domain.usecase.GetCoinOverviewUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveIsFavoriteUseCase
import ru.urfu.cryptoapp.domain.usecase.RemoveFavoriteUseCase
import ru.urfu.cryptoapp.ui.coins.model.CoinDetailsUi
import ru.urfu.cryptoapp.ui.coins.model.CoinUiMapper
import ru.urfu.cryptoapp.ui.navigation.Routes

sealed interface CoinDetailsUiState {
    data object Loading : CoinDetailsUiState
    data class Content(val details: CoinDetailsUi) : CoinDetailsUiState
    data class Error(val error: DataError) : CoinDetailsUiState
}

class CoinDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCoinOverview: GetCoinOverviewUseCase,
    observeIsFavorite: ObserveIsFavoriteUseCase,
    private val addFavorite: AddFavoriteUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
) : ViewModel() {

    /** Аргумент навигации coins/details/{coinId} — он же path-параметр запроса /coins/{id}. */
    private val coinId: String = checkNotNull(savedStateHandle[Routes.COIN_ID_ARG])

    private val _uiState = MutableStateFlow<CoinDetailsUiState>(CoinDetailsUiState.Loading)
    val uiState: StateFlow<CoinDetailsUiState> = _uiState.asStateFlow()

    /** Состояние читается из Room, поэтому работает и без интернета. */
    val isFavorite: StateFlow<Boolean> = observeIsFavorite(coinId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Доменные рыночные данные — не для экрана, а для сохранения в избранное. */
    private var market: CoinMarket? = null

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        _uiState.value = CoinDetailsUiState.Loading
        // Flow может отдать сначала кэш, а затем свежие данные — экран просто перерисуется
        loadJob = getCoinOverview(coinId)
            .onEach {
                market = it.market
                _uiState.value = CoinDetailsUiState.Content(CoinUiMapper.toDetails(it.details, it.market))
            }
            .catch { _uiState.value = CoinDetailsUiState.Error(it.toDataError()) }
            .launchIn(viewModelScope)
    }

    /**
     * Убрать из избранного можно всегда (и без сети), а добавить — только когда
     * загружены рыночные данные: именно они сохраняются в базу.
     */
    fun onFavoriteClick() {
        if (isFavorite.value) {
            viewModelScope.launch { removeFavorite(coinId) }
            return
        }
        val market = market ?: return
        viewModelScope.launch { addFavorite(market) }
    }
}
