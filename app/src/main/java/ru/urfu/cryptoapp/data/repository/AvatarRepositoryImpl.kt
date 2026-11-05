package ru.urfu.cryptoapp.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.urfu.cryptoapp.domain.repository.AvatarRepository
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Аватарки лежат в приватной папке приложения, поэтому разрешения на доступ к файлам не нужны.
 * Имя файла каждый раз новое: Coil кэширует картинки по URI и иначе показал бы старое фото.
 */
class AvatarRepositoryImpl(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AvatarRepository {

    private val appContext = context.applicationContext
    private val avatarsDir = File(appContext.filesDir, "avatars")
    private val cameraDir = File(appContext.cacheDir, "camera")

    override suspend fun createCameraCaptureUri(): String = withContext(ioDispatcher) {
        cameraDir.deleteRecursively() // снимок нужен только до копирования в аватарки
        cameraDir.mkdirs()
        val file = File(cameraDir, "${UUID.randomUUID()}.jpg").apply { createNewFile() }
        FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file).toString()
    }

    override suspend fun importAvatar(sourceUri: String): String = withContext(ioDispatcher) {
        avatarsDir.mkdirs()
        val target = File(avatarsDir, "${UUID.randomUUID()}.jpg")
        try {
            val input = appContext.contentResolver.openInputStream(Uri.parse(sourceUri))
                ?: throw IOException("Cannot open $sourceUri")
            val copied = input.use { source -> target.outputStream().use { source.copyTo(it) } }
            if (copied == 0L) throw IOException("Empty image")
        } catch (e: Exception) {
            target.delete()
            throw e
        }
        Uri.fromFile(target).toString()
    }

    override suspend fun deleteUnusedAvatars(keepUri: String?) = withContext(ioDispatcher) {
        avatarsDir.listFiles().orEmpty()
            .filter { Uri.fromFile(it).toString() != keepUri }
            .forEach { it.delete() }
    }
}
