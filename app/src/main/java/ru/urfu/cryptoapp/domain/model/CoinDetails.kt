package ru.urfu.cryptoapp.domain.model

/**
 * Карточка монеты.
 * Соответствует ответу `GET /coins/{id}?localization=false&tickers=false&market_data=false`
 */
data class CoinDetails(
    val id: String,                          // id
    val symbol: String,                      // symbol
    val name: String,                        // name
    val imageUrl: String,                    // image.large
    val description: String,                 // description.en
    val categories: List<String>,            // categories
    val hashingAlgorithm: String?,           // hashing_algorithm
    val genesisDate: String?,                // genesis_date
    val marketCapRank: Int,                  // market_cap_rank
    val sentimentVotesUpPercentage: Double,  // sentiment_votes_up_percentage
    val watchlistUsers: Int,                 // watchlist_portfolio_users
    val links: CoinLinks,                    // links
)

data class CoinLinks(
    val homepage: String?,       // links.homepage[0]
    val blockchainSite: String?, // links.blockchain_site[0]
    val subreddit: String?,      // links.subreddit_url
    val github: String?,         // links.repos_url.github[0]
)
