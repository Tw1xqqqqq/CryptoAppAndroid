package ru.urfu.cryptoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.cryptoapp.domain.model.DownloadedDocument
import ru.urfu.cryptoapp.domain.model.UserProfile
import ru.urfu.cryptoapp.domain.repository.AvatarRepository
import ru.urfu.cryptoapp.domain.repository.DocumentRepository
import ru.urfu.cryptoapp.domain.repository.ProfileRepository
import java.net.URI
import java.net.URISyntaxException

class ObserveProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile> = repository.profile
}

/** Сохраняет профиль и удаляет аватарки, которые больше не используются. */
class SaveProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val avatarRepository: AvatarRepository,
) {
    suspend operator fun invoke(profile: UserProfile) {
        profileRepository.save(profile)
        avatarRepository.deleteUnusedAvatars(keepUri = profile.avatarUri)
    }
}

/**
 * Приводит введённую ссылку к виду, пригодному для скачивания:
 * «www.hh.ru/cv.pdf» → «https://www.hh.ru/cv.pdf». Пустая ссылка допустима, null — ссылка некорректна.
 * Разрешён только https: незашифрованный трафик Android блокирует по умолчанию.
 */
class NormalizeResumeUrlUseCase {
    operator fun invoke(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        val url = if ("://" in trimmed) trimmed else "https://$trimmed"
        val uri = try {
            URI(url)
        } catch (e: URISyntaxException) {
            return null
        }
        return url.takeIf { uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank() }
    }
}

class CreateCameraCaptureUriUseCase(private val repository: AvatarRepository) {
    suspend operator fun invoke(): String = repository.createCameraCaptureUri()
}

class ImportAvatarUseCase(private val repository: AvatarRepository) {
    suspend operator fun invoke(sourceUri: String): String = repository.importAvatar(sourceUri)
}

class DeleteUnusedAvatarsUseCase(private val repository: AvatarRepository) {
    suspend operator fun invoke(keepUri: String?) = repository.deleteUnusedAvatars(keepUri)
}

class DownloadResumeUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(url: String): DownloadedDocument = repository.download(url)
}
