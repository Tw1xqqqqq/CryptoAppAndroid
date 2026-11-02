package ru.urfu.cryptoapp.ui.coins.model

import androidx.annotation.StringRes

/**
 * UI-модели: всё уже отформатировано во ViewModel, экран только отображает строки.
 * Подписи — ресурсы (@StringRes), чтобы ViewModel не зависела от Context.
 */

/** Изменение цены: текст «+1.84%» и знак, по которому экран выбирает цвет. */
data class PriceChangeUi(
    val text: String,
    val isPositive: Boolean,
)

/** Строка списка монет. */
data class CoinListItemUi(
    val id: String,
    val rank: String,      // "1"
    val name: String,      // "Bitcoin"
    val imageUrl: String,
    val subtitle: String,  // "BTC · $1.23T"
    val price: String,     // "$62,350.00"
    val change: PriceChangeUi,
)

/** Экран деталей монеты. */
data class CoinDetailsUi(
    val name: String,
    val symbol: String,     // "BTC"
    val imageUrl: String,
    val rank: String,       // "#1" или "#—"
    val market: CoinMarketUi?,
    val categories: List<String>,
    val description: String,
    val info: List<LabeledValueUi>,
    val sentiment: SentimentUi?, // null — голосов нет, секция скрывается
    val links: List<LinkUi>,     // пустой — секция скрывается
)

/** Рыночный блок деталей: цена, диапазон за 24ч и сетка показателей. */
data class CoinMarketUi(
    val price: String,
    val change: PriceChangeUi,
    val low24h: String,
    val high24h: String,
    val rangeFraction: Float, // положение текущей цены между минимумом и максимумом, 0..1
    val stats: List<LabeledValueUi>,
)

data class LabeledValueUi(
    @StringRes val label: Int,
    val value: String,
)

data class SentimentUi(
    val upFraction: Float, // 0..1
    val upText: String,    // "▲ 84.2%"
    val downText: String,  // "▼ 15.8%"
)

data class LinkUi(
    @StringRes val title: Int,
    val url: String,
)
