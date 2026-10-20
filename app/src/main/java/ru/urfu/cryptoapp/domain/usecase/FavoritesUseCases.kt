package ru.urfu.cryptoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.repository.FavoritesRepository

class ObserveFavoritesUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(): Flow<List<CoinMarket>> = repository.observeFavorites()
}

class ObserveIsFavoriteUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(coinId: String): Flow<Boolean> = repository.observeIsFavorite(coinId)
}

class AddFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(coin: CoinMarket) = repository.add(coin)
}

class RemoveFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(coinId: String) = repository.remove(coinId)
}
