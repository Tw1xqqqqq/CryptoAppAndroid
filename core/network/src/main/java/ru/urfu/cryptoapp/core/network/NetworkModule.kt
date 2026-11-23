package ru.urfu.cryptoapp.core.network

import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 15L
private const val DOWNLOAD_TIMEOUT_SECONDS = 60L

object NetworkQualifiers {
    /** Общий клиент: Chucker и логирование. Модули строят на его основе свои клиенты. */
    val BASE_CLIENT = named("base_client")

    /** Для скачивания файлов с произвольных сайтов: увеличенный таймаут чтения. */
    val DOWNLOAD_CLIENT = named("download_client")
}

val coreNetworkModule: Module = module {
    single(NetworkQualifiers.BASE_CLIENT) {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // В release подключается no-op версия Chucker
            .addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()
    }
    single(NetworkQualifiers.DOWNLOAD_CLIENT) {
        get<OkHttpClient>(NetworkQualifiers.BASE_CLIENT).newBuilder()
            .readTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
