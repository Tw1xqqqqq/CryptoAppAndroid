package ru.urfu.cryptoapp.data.repository

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.urfu.cryptoapp.data.remote.safeApiCall
import ru.urfu.cryptoapp.domain.model.DataError
import ru.urfu.cryptoapp.domain.model.DataException
import ru.urfu.cryptoapp.domain.model.DownloadedDocument
import ru.urfu.cryptoapp.domain.repository.DocumentRepository
import java.io.File
import java.io.InputStream
import kotlin.coroutines.coroutineContext

/**
 * Скачивает документ в кэш приложения (разрешение на запись не нужно ни на какой версии Android)
 * и отдаёт content-URI, через который файл откроет другое приложение.
 */
class DocumentRepositoryImpl(
    context: Context,
    private val client: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DocumentRepository {

    private val appContext = context.applicationContext
    private val documentsDir = File(appContext.cacheDir, "documents")

    override suspend fun download(url: String): DownloadedDocument = withContext(ioDispatcher) {
        val httpUrl = url.toHttpUrlOrNull() ?: throw DataException(DataError.UNKNOWN)
        safeApiCall {
            client.newCall(Request.Builder().url(httpUrl).build()).execute().use { response ->
                if (!response.isSuccessful) throw DataException(httpError(response.code))

                val body = checkNotNull(response.body)
                val mimeType = resolveMimeType(body.contentType()?.let { "${it.type}/${it.subtype}" }, httpUrl)
                val partial = File(documentsDir, "download.part")
                documentsDir.deleteRecursively()
                documentsDir.mkdirs()
                body.byteStream().use { saveLimited(it, partial) }

                // Имя задаём сами: имя из ссылки нельзя использовать как путь к файлу
                val file = File(documentsDir, "resume" + extensionFor(mimeType, httpUrl))
                check(partial.renameTo(file)) { "Cannot save document" }
                DownloadedDocument(
                    uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file).toString(),
                    mimeType = mimeType,
                )
            }
        }
    }

    private suspend fun saveLimited(input: InputStream, target: File) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        target.outputStream().use { output ->
            while (true) {
                coroutineContext.ensureActive()
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_DOCUMENT_BYTES) throw DataException(DataError.FILE_TOO_LARGE)
                output.write(buffer, 0, read)
            }
        }
    }

    private fun httpError(code: Int): DataError = when (code) {
        404, 410 -> DataError.NOT_FOUND
        in 500..599 -> DataError.SERVER
        else -> DataError.UNKNOWN
    }

    /** Серверы часто отдают файлы как octet-stream — тогда тип определяем по расширению в ссылке. */
    private fun resolveMimeType(contentType: String?, url: HttpUrl): String {
        if (contentType != null && contentType != GENERIC_BINARY) return contentType
        val extension = url.pathSegments.last().substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ANY_TYPE
    }

    private fun extensionFor(mimeType: String, url: HttpUrl): String {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: url.pathSegments.last().substringAfterLast('.', "").lowercase()
        return if (SAFE_EXTENSION.matches(extension)) ".$extension" else ""
    }

    private companion object {
        const val MAX_DOCUMENT_BYTES = 50L * 1024 * 1024
        const val GENERIC_BINARY = "application/octet-stream"
        const val ANY_TYPE = "*/*"
        val SAFE_EXTENSION = Regex("[a-z0-9]{1,8}")
    }
}
