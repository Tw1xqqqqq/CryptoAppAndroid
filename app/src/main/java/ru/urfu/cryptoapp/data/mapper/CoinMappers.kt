package ru.urfu.cryptoapp.data.mapper

import ru.urfu.cryptoapp.data.remote.dto.CoinDetailsDto
import ru.urfu.cryptoapp.data.remote.dto.CoinMarketDto
import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinLinks
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.domain.model.VsCurrency

fun CoinMarketDto.toDomain(currency: VsCurrency) = CoinMarket(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = image.orEmpty(),
    currentPrice = currentPrice ?: 0.0,
    marketCap = marketCap ?: 0.0,
    marketCapRank = marketCapRank ?: 0,
    totalVolume = totalVolume ?: 0.0,
    high24h = high24h ?: 0.0,
    low24h = low24h ?: 0.0,
    priceChangePercentage24h = priceChangePercentage24h ?: 0.0,
    circulatingSupply = circulatingSupply ?: 0.0,
    maxSupply = maxSupply,
    currency = currency,
)

fun CoinDetailsDto.toDomain() = CoinDetails(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = image?.large ?: image?.small ?: image?.thumb.orEmpty(),
    description = description?.en.orEmpty().stripHtml(),
    categories = categories.filterNotNull().filter { it.isNotBlank() },
    hashingAlgorithm = hashingAlgorithm?.takeIf { it.isNotBlank() },
    genesisDate = genesisDate?.takeIf { it.isNotBlank() },
    marketCapRank = marketCapRank,
    sentimentVotesUpPercentage = sentimentVotesUpPercentage,
    watchlistUsers = watchlistPortfolioUsers,
    links = CoinLinks(
        homepage = links?.homepage.firstNotBlank(),
        blockchainSite = links?.blockchainSite.firstNotBlank(),
        subreddit = links?.subredditUrl?.takeIf { it.isNotBlank() },
        github = links?.reposUrl?.github.firstNotBlank(),
    ),
)

/** В списках ссылок CoinGecko часто встречаются пустые строки. */
private fun List<String>?.firstNotBlank(): String? = this?.firstOrNull { it.isNotBlank() }

/** Описание приходит с HTML-ссылками (`<a href="...">`) — оставляем только текст. */
private fun String.stripHtml(): String = this
    .replace(HTML_TAG, "")
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("\r\n", "\n")
    .trim()

private val HTML_TAG = Regex("<[^>]+>")
