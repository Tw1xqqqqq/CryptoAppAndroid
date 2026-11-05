package ru.urfu.cryptoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.domain.model.DownloadedDocument
import ru.urfu.cryptoapp.domain.model.UserProfile

/** Сохранённый профиль. */
interface ProfileRepository {

    /** Текущий профиль; новое значение приходит после каждого [save]. */
    val profile: Flow<UserProfile>

    suspend fun save(profile: UserProfile)
}

/** Файлы аватарок в памяти приложения. */
interface AvatarRepository {

    /** URI пустого файла, в который приложение камеры запишет снимок. */
    suspend fun createCameraCaptureUri(): String

    /** Копирует фото из [sourceUri] к себе и возвращает URI копии: выданный доступ к источнику может истечь. */
    suspend fun importAvatar(sourceUri: String): String

    /** Удаляет все файлы аватарок, кроме [keepUri]. */
    suspend fun deleteUnusedAvatars(keepUri: String?)
}

/** Планирование напоминания о начале пары. */
interface LectureReminderScheduler {

    /** Заменяет прежнее напоминание, если оно было. */
    fun schedule(triggerAtMillis: Long, ownerName: String)

    fun cancel()
}

/** Скачивание документов из сети. */
interface DocumentRepository {

    /** Бросает [ru.urfu.cryptoapp.domain.model.DataException], если скачать не удалось. */
    suspend fun download(url: String): DownloadedDocument
}
