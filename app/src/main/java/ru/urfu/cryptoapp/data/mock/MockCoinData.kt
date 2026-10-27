package ru.urfu.cryptoapp.data.mock

import ru.urfu.cryptoapp.domain.model.CoinDetails
import ru.urfu.cryptoapp.domain.model.CoinLinks
import ru.urfu.cryptoapp.domain.model.CoinMarket

/** Моковые данные в формате CoinGecko. Значения приблизительные. */
object MockCoinData {

    private const val IMG = "https://coin-images.coingecko.com/coins/images"

    val markets: List<CoinMarket> = listOf(
        market(1, "bitcoin", "btc", "Bitcoin", "$IMG/1/large/bitcoin.png", 62_350.0, 1.23e12, 3.1e10, 63_100.0, 61_200.0, 1.84, 19_760_000.0, 21_000_000.0),
        market(2, "ethereum", "eth", "Ethereum", "$IMG/279/large/ethereum.png", 2_450.0, 2.95e11, 1.4e10, 2_510.0, 2_400.0, -0.92, 120_350_000.0, null),
        market(3, "tether", "usdt", "Tether", "$IMG/325/large/Tether.png", 1.0, 1.19e11, 4.5e10, 1.001, 0.998, 0.01, 119_500_000_000.0, null),
        market(4, "binancecoin", "bnb", "BNB", "$IMG/825/large/bnb-icon2_2x.png", 565.3, 8.25e10, 1.2e9, 572.0, 558.0, 0.75, 145_900_000.0, 200_000_000.0),
        market(5, "solana", "sol", "Solana", "$IMG/4128/large/solana.png", 145.7, 6.84e10, 2.3e9, 149.9, 141.2, 3.12, 469_500_000.0, null),
        market(6, "usd-coin", "usdc", "USDC", "$IMG/6319/large/usdc.png", 1.0, 3.58e10, 5.6e9, 1.002, 0.999, -0.02, 35_800_000_000.0, null),
        market(7, "ripple", "xrp", "XRP", "$IMG/44/large/xrp-symbol-white-128.png", 0.5321, 3.01e10, 1.1e9, 0.5412, 0.5250, -1.45, 56_600_000_000.0, 100_000_000_000.0),
        market(8, "dogecoin", "doge", "Dogecoin", "$IMG/5/large/dogecoin.png", 0.1102, 1.61e10, 6.4e8, 0.1145, 0.1071, 4.87, 146_200_000_000.0, null),
        market(9, "tron", "trx", "TRON", "$IMG/1094/large/tron-logo.png", 0.1563, 1.35e10, 3.2e8, 0.1580, 0.1550, 0.33, 86_500_000_000.0, null),
        market(10, "cardano", "ada", "Cardano", "$IMG/975/large/cardano.png", 0.3487, 1.22e10, 2.9e8, 0.3560, 0.3420, -2.11, 35_000_000_000.0, 45_000_000_000.0),
        market(11, "avalanche-2", "avax", "Avalanche", "$IMG/12559/large/Avalanche_Circle_RedWhite_Trans.png", 26.41, 1.07e10, 2.8e8, 27.30, 25.90, 1.05, 406_000_000.0, 720_000_000.0),
        market(12, "shiba-inu", "shib", "Shiba Inu", "$IMG/11939/large/shiba.png", 0.00001742, 1.03e10, 3.6e8, 0.00001801, 0.00001700, 2.64, 589_260_000_000_000.0, null),
        market(13, "chainlink", "link", "Chainlink", "$IMG/877/large/chainlink-new-logo.png", 11.08, 6.94e9, 2.4e8, 11.42, 10.85, -0.58, 626_800_000.0, 1_000_000_000.0),
        market(14, "polkadot", "dot", "Polkadot", "$IMG/12171/large/polkadot.png", 4.32, 6.43e9, 1.5e8, 4.45, 4.25, -1.21, 1_490_000_000.0, null),
        market(15, "bitcoin-cash", "bch", "Bitcoin Cash", "$IMG/780/large/bitcoin-cash-circle.png", 325.6, 6.43e9, 2.1e8, 331.0, 318.4, 0.94, 19_780_000.0, 21_000_000.0),
        market(16, "near", "near", "NEAR Protocol", "$IMG/10365/large/near.jpg", 4.81, 5.62e9, 2.7e8, 5.02, 4.70, 5.36, 1_170_000_000.0, null),
        market(17, "litecoin", "ltc", "Litecoin", "$IMG/2/large/litecoin.png", 65.3, 4.9e9, 2.9e8, 66.8, 64.1, 0.42, 75_100_000.0, 84_000_000.0),
        market(18, "uniswap", "uni", "Uniswap", "$IMG/12504/large/uni.jpg", 7.26, 4.36e9, 1.2e8, 7.51, 7.10, -3.07, 600_400_000.0, 1_000_000_000.0),
        market(19, "internet-computer", "icp", "Internet Computer", "$IMG/14495/large/Internet_Computer_logo.png", 8.62, 4.07e9, 6.9e7, 8.88, 8.41, 1.77, 472_000_000.0, null),
        market(20, "stellar", "xlm", "Stellar", "$IMG/100/large/Stellar_symbol_black_RGB.png", 0.0912, 2.69e9, 5.3e7, 0.0931, 0.0899, -0.36, 29_500_000_000.0, 50_001_806_812.0),
    )

    private class Extra(
        val description: String,
        val categories: List<String>,
        val algorithm: String? = null,
        val genesis: String? = null,
        val homepage: String? = null,
        val explorer: String? = null,
        val reddit: String? = null,
        val github: String? = null,
        val sentimentUp: Double = 70.0,
        val watchlist: Int = 100_000,
    )

    private val extras: Map<String, Extra> = mapOf(
        "bitcoin" to Extra(
            description = "Bitcoin is the first successful internet money based on peer-to-peer technology; " +
                "whereby no central bank or authority is involved in the transaction and production of the Bitcoin currency. " +
                "It was created by an anonymous individual/group under the name, Satoshi Nakamoto. The source code is available " +
                "publicly as an open source project, anybody can look at it and be part of the developmental process.\n\n" +
                "Bitcoin is changing the way we see money as we speak. The idea was to produce a means of exchange, independent " +
                "of any central authority, that could be transferred electronically in a secure, verifiable and immutable way.",
            categories = listOf("Cryptocurrency", "Layer 1 (L1)", "Proof of Work (PoW)", "Smart Contract Platform"),
            algorithm = "SHA-256",
            genesis = "2009-01-03",
            homepage = "https://bitcoin.org",
            explorer = "https://mempool.space",
            reddit = "https://www.reddit.com/r/Bitcoin/",
            github = "https://github.com/bitcoin/bitcoin",
            sentimentUp = 84.2,
            watchlist = 1_780_000,
        ),
        "ethereum" to Extra(
            description = "Ethereum is a global, open-source platform for decentralized applications. In other words, the vision " +
                "is to create a world computer that anyone can build applications in a decentralized manner; while all states " +
                "and data are distributed and publicly accessible. Ethereum supports smart contracts in which developers can " +
                "write code in order to program digital value.\n\n" +
                "Ether (ETH) is the native currency of the Ethereum network and is used to pay transaction fees (gas).",
            categories = listOf("Smart Contract Platform", "Layer 1 (L1)", "Proof of Stake (PoS)"),
            algorithm = "Ethash",
            genesis = "2015-07-30",
            homepage = "https://www.ethereum.org",
            explorer = "https://etherscan.io",
            reddit = "https://www.reddit.com/r/ethereum",
            github = "https://github.com/ethereum/go-ethereum",
            sentimentUp = 79.5,
            watchlist = 1_320_000,
        ),
        "solana" to Extra(
            description = "Solana is a Layer 1 blockchain that offers users fast speeds and affordable costs. It supports smart " +
                "contracts and facilitates the creation of decentralized applications. Solana uses a combination of " +
                "Proof of History and Proof of Stake consensus mechanisms.",
            categories = listOf("Smart Contract Platform", "Layer 1 (L1)", "Solana Ecosystem"),
            genesis = "2020-03-16",
            homepage = "https://solana.com",
            explorer = "https://solscan.io",
            reddit = "https://www.reddit.com/r/solana",
            github = "https://github.com/solana-labs/solana",
            sentimentUp = 81.0,
            watchlist = 820_000,
        ),
        "dogecoin" to Extra(
            description = "Dogecoin is a cryptocurrency based on the popular \"Doge\" Internet meme and features a Shiba Inu on its logo. " +
                "It was created by Billy Markus and Jackson Palmer as a fun alternative to Bitcoin.",
            categories = listOf("Meme", "Proof of Work (PoW)", "Dog-Themed"),
            algorithm = "Scrypt",
            genesis = "2013-12-08",
            homepage = "https://dogecoin.com",
            explorer = "https://dogechain.info",
            reddit = "https://www.reddit.com/r/dogecoin/",
            github = "https://github.com/dogecoin/dogecoin",
            sentimentUp = 76.3,
            watchlist = 610_000,
        ),
        "cardano" to Extra(
            description = "Cardano is a proof-of-stake blockchain platform that says its goal is to allow \"changemakers, innovators " +
                "and visionaries\" to bring about positive global change. ADA is its native token.",
            categories = listOf("Smart Contract Platform", "Layer 1 (L1)", "Proof of Stake (PoS)"),
            genesis = "2017-09-23",
            homepage = "https://cardano.org",
            explorer = "https://cardanoscan.io",
            reddit = "https://www.reddit.com/r/cardano",
            github = "https://github.com/IntersectMBO/cardano-node",
            sentimentUp = 72.4,
            watchlist = 540_000,
        ),
        "litecoin" to Extra(
            description = "Litecoin is a peer-to-peer cryptocurrency created by Charlie Lee. It was created based on the Bitcoin " +
                "protocol but differs in terms of the hashing algorithm used and has a faster block time of 2.5 minutes.",
            categories = listOf("Cryptocurrency", "Proof of Work (PoW)", "Layer 1 (L1)"),
            algorithm = "Scrypt",
            genesis = "2011-10-07",
            homepage = "https://litecoin.org",
            explorer = "https://blockchair.com/litecoin",
            reddit = "https://www.reddit.com/r/litecoin/",
            github = "https://github.com/litecoin-project/litecoin",
            sentimentUp = 74.0,
            watchlist = 330_000,
        ),
    )

    fun details(id: String): CoinDetails? {
        val market = markets.firstOrNull { it.id == id } ?: return null
        val extra = extras[id] ?: Extra(
            description = "${market.name} (${market.symbol.uppercase()}) is a cryptocurrency ranked " +
                "#${market.marketCapRank} by market capitalization. Detailed description will be loaded " +
                "from CoinGecko API in the next practice.",
            categories = listOf("Cryptocurrency"),
            homepage = "https://www.coingecko.com/en/coins/${market.id}",
        )
        return CoinDetails(
            id = market.id,
            symbol = market.symbol,
            name = market.name,
            imageUrl = market.imageUrl,
            description = extra.description,
            categories = extra.categories,
            hashingAlgorithm = extra.algorithm,
            genesisDate = extra.genesis,
            marketCapRank = market.marketCapRank,
            sentimentVotesUpPercentage = extra.sentimentUp,
            watchlistUsers = extra.watchlist,
            links = CoinLinks(
                homepage = extra.homepage,
                blockchainSite = extra.explorer,
                subreddit = extra.reddit,
                github = extra.github,
            ),
        )
    }

    private fun market(
        rank: Int, id: String, symbol: String, name: String, image: String,
        price: Double, marketCap: Double, volume: Double, high: Double, low: Double,
        change: Double, circulating: Double, maxSupply: Double?,
    ) = CoinMarket(
        id = id,
        symbol = symbol,
        name = name,
        imageUrl = image,
        currentPrice = price,
        marketCap = marketCap,
        marketCapRank = rank,
        totalVolume = volume,
        high24h = high,
        low24h = low,
        priceChangePercentage24h = change,
        circulatingSupply = circulating,
        maxSupply = maxSupply,
    )
}
