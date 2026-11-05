package ru.urfu.cryptoapp.data.local.profile

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.urfu.cryptoapp.domain.model.UserProfile
import ru.urfu.cryptoapp.domain.repository.ProfileRepository
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Профиль — одна запись без выборок, поэтому Room избыточна, а в Preferences пришлось бы вести ключ на каждое поле.
 * Типизированный DataStore хранит объект целиком в JSON-файле и записывает его атомарно.
 */
@Serializable
data class ProfileDto(
    val fullName: String = "",
    val position: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = "",
    val lectureTime: String = "",
)

object ProfileSerializer : Serializer<ProfileDto> {

    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue = ProfileDto()

    override suspend fun readFrom(input: InputStream): ProfileDto = try {
        json.decodeFromString(input.readBytes().decodeToString())
    } catch (e: SerializationException) {
        throw CorruptionException("Cannot read profile", e)
    }

    override suspend fun writeTo(t: ProfileDto, output: OutputStream) {
        output.write(json.encodeToString(t).encodeToByteArray())
    }
}

class ProfileRepositoryImpl(
    private val dataStore: DataStore<ProfileDto>,
) : ProfileRepository {

    override val profile: Flow<UserProfile> = dataStore.data
        .catch { e -> if (e is IOException) emit(ProfileSerializer.defaultValue) else throw e }
        .map { it.toDomain() }

    override suspend fun save(profile: UserProfile) {
        dataStore.updateData { profile.toDto() }
    }
}

private fun ProfileDto.toDomain() = UserProfile(
    fullName = fullName,
    position = position,
    avatarUri = avatarUri,
    resumeUrl = resumeUrl,
    lectureTime = lectureTime,
)

private fun UserProfile.toDto() = ProfileDto(
    fullName = fullName,
    position = position,
    avatarUri = avatarUri,
    resumeUrl = resumeUrl,
    lectureTime = lectureTime,
)
