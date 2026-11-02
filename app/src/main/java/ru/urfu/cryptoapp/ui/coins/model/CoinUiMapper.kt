package ru.urfu.cryptoapp.ui.coins.model

import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.ui.common.formatCompact
import ru.urfu.cryptoapp.ui.common.formatCompactUsd
import ru.urfu.cryptoapp.ui.common.formatPercent
import ru.urfu.cryptoapp.ui.common.formatPrice
import java.util.Locale

/** Преобразование доменных моделей в UI-модели. Вызывается из ViewModel. */
object CoinUiMapper {

    private const val NOT_LIMITED = "∞"
    private const val NO_VALUE = "—"

    fun toListItem(coin: CoinMarket) = CoinListItemUi(
        id = coin.id,
        rank = coin.marketCapRank.toString(),
        name = coin.name,
        imageUrl = coin.imageUrl,
        subtitle = "${coin.symbol.uppercase()} · ${coin.marketCap.formatCompactUsd()}",
        price = coin.currentPrice.formatPrice(),
        change = priceChange(coin.priceChangePercentage24h),
    )

    fun toDetails(details: CoinDetails, market: CoinMarket?) = CoinDetailsUi(
        name = details.name,
        symbol = details.symbol.uppercase(),
        imageUrl = details.imageUrl,
        rank = "#${details.marketCapRank ?: NO_VALUE}",
        market = market?.let(::toMarket),
        categories = details.categories,
        description = details.description,
        info = listOf(
            LabeledValueUi(R.string.hashing_algorithm, details.hashingAlgorithm ?: NO_VALUE),
            LabeledValueUi(R.string.genesis_date, details.genesisDate ?: NO_VALUE),
            LabeledValueUi(R.string.watchlist_users, details.watchlistUsers?.toDouble()?.formatCompact() ?: NO_VALUE),
        ),
        sentiment = details.sentimentVotesUpPercentage?.let(::sentiment),
        links = listOfNotNull(
            details.links.homepage?.let { LinkUi(R.string.link_homepage, it) },
            details.links.blockchainSite?.let { LinkUi(R.string.link_explorer, it) },
            details.links.subreddit?.let { LinkUi(R.string.link_reddit, it) },
            details.links.github?.let { LinkUi(R.string.link_github, it) },
        ),
    )

    private fun toMarket(market: CoinMarket): CoinMarketUi {
        val span = (market.high24h - market.low24h).takeIf { it > 0 } ?: 1.0
        return CoinMarketUi(
            price = market.currentPrice.formatPrice(),
            change = priceChange(market.priceChangePercentage24h),
            low24h = market.low24h.formatPrice(),
            high24h = market.high24h.formatPrice(),
            rangeFraction = ((market.currentPrice - market.low24h) / span).toFloat().coerceIn(0f, 1f),
            stats = listOf(
                LabeledValueUi(R.string.market_cap, market.marketCap.formatCompactUsd()),
                LabeledValueUi(R.string.volume_24h, market.totalVolume.formatCompactUsd()),
                LabeledValueUi(R.string.circulating_supply, market.circulatingSupply.formatCompact()),
                LabeledValueUi(R.string.max_supply, market.maxSupply?.formatCompact() ?: NOT_LIMITED),
                LabeledValueUi(R.string.high_24h, market.high24h.formatPrice()),
                LabeledValueUi(R.string.low_24h, market.low24h.formatPrice()),
            ),
        )
    }

    private fun priceChange(percent: Double) = PriceChangeUi(
        text = percent.formatPercent(),
        isPositive = percent >= 0,
    )

    private fun sentiment(upPercentage: Double): SentimentUi {
        val up = upPercentage.coerceIn(0.0, 100.0)
        return SentimentUi(
            upFraction = (up / 100).toFloat(),
            upText = "▲ %.1f%%".format(Locale.US, up),
            downText = "▼ %.1f%%".format(Locale.US, 100 - up),
        )
    }
}
