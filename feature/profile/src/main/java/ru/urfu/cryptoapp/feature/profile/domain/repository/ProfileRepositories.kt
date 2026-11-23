package ru.urfu.cryptoapp.feature.profile.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.feature.profile.domain.model.DownloadedDocument
import ru.urfu.cryptoapp.feature.profile.domain.model.UserProfile

/** Сохранённый профиль. */
internal interface ProfileRepository {

    /** Текущий профиль; новое значение приходит после каждого [save]. */
    val profile: Flow<UserProfile>

    suspend fun save(profile: UserProfile)
}

/** Файлы аватарок в памяти приложения. */
internal interface AvatarRepository {

    /** URI пустого файла, в который приложение камеры запишет снимок. */
    suspend fun createCameraCaptureUri(): String

    /** Копирует фото из [sourceUri] к себе и возвращает URI копии: выданный доступ к источнику может истечь. */
    suspend fun importAvatar(sourceUri: String): String

    /** Удаляет все файлы аватарок, кроме [keepUri]. */
    suspend fun deleteUnusedAvatars(keepUri: String?)
}

/** Планирование напоминания о начале пары. */
internal interface LectureReminderScheduler {

    /** Заменяет прежнее напоминание, если оно было. */
    fun schedule(triggerAtMillis: Long, ownerName: String)

    fun cancel()
}

/** Скачивание документов из сети. */
internal interface DocumentRepository {

    /** Бросает [ru.urfu.cryptoapp.core.domain.DataException], если скачать не удалось. */
    suspend fun download(url: String): DownloadedDocument
}
