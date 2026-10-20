package ru.urfu.cryptoapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.usecase.ObserveFavoritesUseCase

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Content(val coins: List<CoinMarket>) : FavoritesUiState
}

/** Избранное читается только из Room — сеть не нужна. */
class FavoritesViewModel(
    observeFavorites: ObserveFavoritesUseCase,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = observeFavorites()
        .map<List<CoinMarket>, FavoritesUiState> { FavoritesUiState.Content(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState.Loading)
}
