package ru.urfu.cryptoapp.domain.model

import kotlin.coroutines.cancellation.CancellationException

/** Причины, по которым не удалось получить данные. Не зависят от Retrofit/OkHttp. */
enum class DataError {
    NETWORK,    // нет сети / таймаут
    RATE_LIMIT, // HTTP 429 — исчерпан лимит запросов CoinGecko
    NOT_FOUND,  // HTTP 404
    SERVER,     // HTTP 5xx
    UNKNOWN,    // всё остальное (в т.ч. неожиданный формат ответа)
}

/** Единственный тип исключения, который слой данных отдаёт наружу. */
class DataException(val error: DataError, cause: Throwable? = null) : Exception(error.name, cause)

fun Throwable.toDataError(): DataError {
    if (this is CancellationException) throw this
    return (this as? DataException)?.error ?: DataError.UNKNOWN
}
