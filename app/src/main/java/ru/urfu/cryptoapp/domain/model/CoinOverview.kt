package ru.urfu.cryptoapp.domain.model

/** Всё, что нужно экрану деталей: карточка монеты + рыночные данные (могут отсутствовать). */
data class CoinOverview(
    val details: CoinDetails,
    val market: CoinMarket?,
)
