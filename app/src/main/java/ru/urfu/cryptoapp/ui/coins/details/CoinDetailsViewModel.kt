package ru.urfu.cryptoapp.ui.coins.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.urfu.cryptoapp.CryptoApplication
import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.model.DataError
import ru.urfu.cryptoapp.domain.model.toDataError
import ru.urfu.cryptoapp.domain.usecase.GetCoinOverviewUseCase
import ru.urfu.cryptoapp.ui.navigation.Routes

sealed interface CoinDetailsUiState {
    data object Loading : CoinDetailsUiState
    data class Content(val details: CoinDetails, val market: CoinMarket?) : CoinDetailsUiState
    data class Error(val error: DataError) : CoinDetailsUiState
}

class CoinDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCoinOverview: GetCoinOverviewUseCase,
) : ViewModel() {

    /** Аргумент навигации coins/details/{coinId} — он же path-параметр запроса /coins/{id}. */
    private val coinId: String = checkNotNull(savedStateHandle[Routes.COIN_ID_ARG])

    private val _uiState = MutableStateFlow<CoinDetailsUiState>(CoinDetailsUiState.Loading)
    val uiState: StateFlow<CoinDetailsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        _uiState.value = CoinDetailsUiState.Loading
        // Flow может отдать сначала кэш, а затем свежие данные — экран просто перерисуется
        loadJob = getCoinOverview(coinId)
            .onEach { _uiState.value = CoinDetailsUiState.Content(it.details, it.market) }
            .catch { _uiState.value = CoinDetailsUiState.Error(it.toDataError()) }
            .launchIn(viewModelScope)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CryptoApplication
                CoinDetailsViewModel(createSavedStateHandle(), app.container.getCoinOverviewUseCase)
            }
        }
    }
}
