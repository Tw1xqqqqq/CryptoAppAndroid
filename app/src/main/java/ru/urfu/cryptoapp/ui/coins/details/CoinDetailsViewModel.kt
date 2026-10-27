package ru.urfu.cryptoapp.ui.coins.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.di.AppContainer
import ru.urfu.cryptoapp.domain.repository.CoinRepository
import ru.urfu.cryptoapp.ui.coins.model.CoinDetailsUi
import ru.urfu.cryptoapp.ui.coins.model.CoinUiMapper
import ru.urfu.cryptoapp.ui.navigation.Routes

sealed interface CoinDetailsUiState {
    data object Loading : CoinDetailsUiState
    data class Content(val details: CoinDetailsUi) : CoinDetailsUiState
    data class Error(val message: String) : CoinDetailsUiState
}

class CoinDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CoinRepository,
) : ViewModel() {

    /** Аргумент навигации coins/details/{coinId}. */
    private val coinId: String = checkNotNull(savedStateHandle[Routes.COIN_ID_ARG])

    private val _uiState = MutableStateFlow<CoinDetailsUiState>(CoinDetailsUiState.Loading)
    val uiState: StateFlow<CoinDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = CoinDetailsUiState.Loading
        viewModelScope.launch {
            _uiState.value = runCatching {
                val details = repository.getCoinDetails(coinId)
                val market = repository.getCoinMarket(coinId)
                CoinDetailsUiState.Content(CoinUiMapper.toDetails(details, market))
            }.getOrElse { CoinDetailsUiState.Error(it.message.orEmpty()) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CoinDetailsViewModel(createSavedStateHandle(), AppContainer.coinRepository)
            }
        }
    }
}
