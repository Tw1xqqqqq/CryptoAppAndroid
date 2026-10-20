package ru.urfu.cryptoapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.urfu.cryptoapp.data.remote.dto.CoinDetailsDto
import ru.urfu.cryptoapp.data.remote.dto.CoinMarketDto

/** CoinGecko API v3. Документация: https://docs.coingecko.com/reference/introduction */
interface CoinGeckoApi {

    /** Пример: `coins/markets?vs_currency=usd&order=market_cap_desc&per_page=10&page=1` */
    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") vsCurrency: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
        @Query("order") order: String = "market_cap_desc",
        @Query("category") category: String? = null,
        @Query("ids") ids: String? = null, // null — параметр не попадёт в запрос
    ): List<CoinMarketDto>

    /** Пример: `coins/bitcoin?localization=false&tickers=false&market_data=false` */
    @GET("coins/{id}")
    suspend fun getCoinDetails(
        @Path("id") id: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = false,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
    ): CoinDetailsDto
}
