package ru.urfu.cryptoapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Добавляет демо-ключ CoinGecko, если он задан. Без ключа запросы тоже проходят, но лимит ниже. */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (apiKey.isBlank()) return chain.proceed(chain.request())
        val request = chain.request().newBuilder()
            .header(HEADER, apiKey)
            .build()
        return chain.proceed(request)
    }

    private companion object {
        const val HEADER = "x-cg-demo-api-key"
    }
}
