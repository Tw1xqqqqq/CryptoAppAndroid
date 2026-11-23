package ru.urfu.cryptoapp.feature.profile.domain.model

/** Профиль пользователя. Хранится только на устройстве. */
internal data class UserProfile(
    val fullName: String = "",
    val position: String = "",
    val avatarUri: String? = null, // URI файла аватарки на устройстве
    val resumeUrl: String = "",    // ссылка на файл резюме/портфолио в сети
    val lectureTime: String = "",  // время любимой пары, HH:mm
)

/** Скачанный документ, готовый к открытию в другом приложении. */
internal data class DownloadedDocument(
    val uri: String,
    val mimeType: String,
)
