package ru.urfu.cryptoapp.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import ru.urfu.cryptoapp.BuildConfig
import ru.urfu.cryptoapp.data.remote.ApiKeyInterceptor
import ru.urfu.cryptoapp.data.remote.CoinGeckoApi
import ru.urfu.cryptoapp.data.repository.CoinRepositoryImpl
import ru.urfu.cryptoapp.domain.repository.CoinRepository
import ru.urfu.cryptoapp.domain.usecase.GetCoinMarketsUseCase
import ru.urfu.cryptoapp.domain.usecase.GetCoinOverviewUseCase
import java.util.concurrent.TimeUnit

/** Простейший ручной DI: живёт в [ru.urfu.cryptoapp.CryptoApplication], собирает граф зависимостей. */
class AppContainer(context: Context) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(ApiKeyInterceptor(BuildConfig.COINGECKO_API_KEY))
        // В release подключается no-op версия Chucker
        .addInterceptor(ChuckerInterceptor.Builder(context).build())
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val api: CoinGeckoApi = Retrofit.Builder()
        .baseUrl(BuildConfig.COINGECKO_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()

    private val coinRepository: CoinRepository by lazy { CoinRepositoryImpl(api) }

    val getCoinMarketsUseCase by lazy { GetCoinMarketsUseCase(coinRepository) }
    val getCoinOverviewUseCase by lazy { GetCoinOverviewUseCase(coinRepository) }

    private companion object {
        const val TIMEOUT_SECONDS = 15L
    }
}
