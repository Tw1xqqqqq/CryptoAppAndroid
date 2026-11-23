package ru.urfu.cryptoapp.core.network

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import ru.urfu.cryptoapp.core.domain.DataError
import ru.urfu.cryptoapp.core.domain.DataException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Выполняет запрос и переводит исключения Retrofit/OkHttp в доменный [DataException]. */
suspend fun <T> safeApiCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val error = when (e.code()) {
        429 -> DataError.RATE_LIMIT
        404 -> DataError.NOT_FOUND
        in 500..599 -> DataError.SERVER
        else -> DataError.UNKNOWN
    }
    throw DataException(error, e)
} catch (e: IOException) {
    throw DataException(DataError.NETWORK, e)
} catch (e: SerializationException) {
    throw DataException(DataError.UNKNOWN, e)
}
