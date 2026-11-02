package ru.urfu.cryptoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ответ `GET /coins/{id}` (только используемые поля, остальные игнорируются). */
@Serializable
data class CoinDetailsDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: ImageDto? = null,
    val description: DescriptionDto? = null,
    val categories: List<String?> = emptyList(),
    @SerialName("hashing_algorithm") val hashingAlgorithm: String? = null,
    @SerialName("genesis_date") val genesisDate: String? = null,
    @SerialName("market_cap_rank") val marketCapRank: Int? = null,
    @SerialName("sentiment_votes_up_percentage") val sentimentVotesUpPercentage: Double? = null,
    @SerialName("watchlist_portfolio_users") val watchlistPortfolioUsers: Int? = null,
    val links: LinksDto? = null,
)

@Serializable
data class ImageDto(
    val thumb: String? = null,
    val small: String? = null,
    val large: String? = null,
)

@Serializable
data class DescriptionDto(
    val en: String? = null,
)

@Serializable
data class LinksDto(
    val homepage: List<String> = emptyList(),
    @SerialName("blockchain_site") val blockchainSite: List<String> = emptyList(),
    @SerialName("subreddit_url") val subredditUrl: String? = null,
    @SerialName("repos_url") val reposUrl: ReposUrlDto? = null,
)

@Serializable
data class ReposUrlDto(
    val github: List<String> = emptyList(),
)
