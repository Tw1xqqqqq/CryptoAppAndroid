package ru.urfu.cryptoapp.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.model.LectureTime
import ru.urfu.cryptoapp.domain.model.UserProfile
import ru.urfu.cryptoapp.domain.usecase.CreateCameraCaptureUriUseCase
import ru.urfu.cryptoapp.domain.usecase.DeleteUnusedAvatarsUseCase
import ru.urfu.cryptoapp.domain.usecase.ImportAvatarUseCase
import ru.urfu.cryptoapp.domain.usecase.NormalizeResumeUrlUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveProfileUseCase
import ru.urfu.cryptoapp.domain.usecase.SaveProfileUseCase
import ru.urfu.cryptoapp.domain.usecase.ScheduleLectureReminderUseCase

data class EditProfileUiState(
    val isLoaded: Boolean = false,
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val lectureTime: String = "",
    val avatarUri: String? = null,
    val isResumeUrlInvalid: Boolean = false,
    val isBusy: Boolean = false,          // копируется фото или сохраняется профиль
    val cameraUriToLaunch: String? = null, // команда экрану: один раз запустить камеру с этим URI
    val isSaved: Boolean = false,         // сохранено — экран можно закрывать
    @StringRes val savedMessage: Int? = null, // итог установки напоминания, показывается при закрытии
    @StringRes val message: Int? = null,
) {
    /** Пустое поле или не HH:mm — показываем ошибку, сохранение недоступно. */
    val isLectureTimeInvalid: Boolean get() = LectureTime.parse(lectureTime) == null

    val canSave: Boolean get() = isLoaded && !isBusy && !isLectureTimeInvalid

    /** С какого времени открыть часы. */
    val pickerTime: LectureTime get() = LectureTime.parse(lectureTime) ?: LectureTime(hour = 12, minute = 0)
}

class EditProfileViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val normalizeResumeUrl: NormalizeResumeUrlUseCase,
    private val createCameraCaptureUri: CreateCameraCaptureUriUseCase,
    private val importAvatar: ImportAvatarUseCase,
    private val deleteUnusedAvatars: DeleteUnusedAvatarsUseCase,
    private val scheduleLectureReminder: ScheduleLectureReminderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        // Черновик живёт в SavedStateHandle: пока открыта камера, система может выгрузить приложение
        if (savedStateHandle.get<Boolean>(KEY_INITIALIZED) == true) {
            _uiState.update {
                it.copy(
                    isLoaded = true,
                    fullName = savedStateHandle[KEY_FULL_NAME] ?: "",
                    position = savedStateHandle[KEY_POSITION] ?: "",
                    resumeUrl = savedStateHandle[KEY_RESUME_URL] ?: "",
                    lectureTime = savedStateHandle[KEY_LECTURE_TIME] ?: "",
                    avatarUri = savedStateHandle[KEY_AVATAR_URI],
                )
            }
        } else {
            viewModelScope.launch {
                val profile = observeProfile().first()
                // Фото из прошлого редактирования, закрытого без сохранения
                runCatching { deleteUnusedAvatars(keepUri = profile.avatarUri) }
                _uiState.update {
                    it.copy(
                        isLoaded = true,
                        fullName = profile.fullName,
                        position = profile.position,
                        resumeUrl = profile.resumeUrl,
                        lectureTime = profile.lectureTime,
                        avatarUri = profile.avatarUri,
                    )
                }
                saveDraft()
                savedStateHandle[KEY_INITIALIZED] = true
            }
        }
    }

    fun onFullNameChange(value: String) = editDraft { it.copy(fullName = value) }

    fun onPositionChange(value: String) = editDraft { it.copy(position = value) }

    fun onResumeUrlChange(value: String) = editDraft { it.copy(resumeUrl = value, isResumeUrlInvalid = false) }

    fun onLectureTimeChange(value: String) = editDraft { it.copy(lectureTime = value) }

    fun onLectureTimePicked(hour: Int, minute: Int) = onLectureTimeChange(LectureTime(hour, minute).format())

    fun onGalleryPhotoPicked(uri: String) = import(uri)

    fun onCameraChosen() {
        viewModelScope.launch {
            try {
                val uri = createCameraCaptureUri()
                savedStateHandle[KEY_CAMERA_URI] = uri
                _uiState.update { it.copy(cameraUriToLaunch = uri) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(message = R.string.profile_photo_failed) }
            }
        }
    }

    fun onCameraLaunched() {
        _uiState.update { it.copy(cameraUriToLaunch = null) }
    }

    fun onCameraUnavailable() {
        _uiState.update { it.copy(cameraUriToLaunch = null, message = R.string.profile_no_camera) }
    }

    fun onCameraResult(success: Boolean) {
        val uri = savedStateHandle.get<String>(KEY_CAMERA_URI)
        savedStateHandle.remove<String>(KEY_CAMERA_URI)
        if (success && uri != null) import(uri)
    }

    /** Проверяет форму до запроса разрешения на уведомления: при ошибке спрашивать его незачем. */
    fun validateForm(): Boolean {
        val state = _uiState.value
        if (!state.canSave) return false
        val isResumeUrlValid = normalizeResumeUrl(state.resumeUrl) != null
        _uiState.update { it.copy(isResumeUrlInvalid = !isResumeUrlValid) }
        return isResumeUrlValid
    }

    fun save(notificationsAllowed: Boolean) {
        val state = _uiState.value
        if (!state.canSave) return
        val lectureTime = LectureTime.parse(state.lectureTime) ?: return
        val resumeUrl = normalizeResumeUrl(state.resumeUrl) ?: return
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                saveProfile(
                    UserProfile(
                        fullName = state.fullName.trim(),
                        position = state.position.trim(),
                        avatarUri = state.avatarUri,
                        resumeUrl = resumeUrl,
                        lectureTime = lectureTime.format(),
                    ),
                )
                val isScheduled = scheduleLectureReminder(lectureTime, ownerName = state.fullName.trim())
                val savedMessage = when {
                    !isScheduled -> R.string.reminder_time_passed
                    !notificationsAllowed -> R.string.reminder_notifications_denied
                    else -> R.string.reminder_set
                }
                _uiState.update { it.copy(isBusy = false, isSaved = true, savedMessage = savedMessage) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isBusy = false, message = R.string.profile_save_failed) }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun import(sourceUri: String) {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                val avatarUri = importAvatar(sourceUri)
                _uiState.update { it.copy(isBusy = false, avatarUri = avatarUri) }
                saveDraft()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isBusy = false, message = R.string.profile_photo_failed) }
            }
        }
    }

    private fun editDraft(transform: (EditProfileUiState) -> EditProfileUiState) {
        _uiState.update(transform)
        saveDraft()
    }

    private fun saveDraft() {
        val state = _uiState.value
        savedStateHandle[KEY_FULL_NAME] = state.fullName
        savedStateHandle[KEY_POSITION] = state.position
        savedStateHandle[KEY_RESUME_URL] = state.resumeUrl
        savedStateHandle[KEY_LECTURE_TIME] = state.lectureTime
        savedStateHandle[KEY_AVATAR_URI] = state.avatarUri
    }

    private companion object {
        const val KEY_INITIALIZED = "initialized"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_POSITION = "position"
        const val KEY_RESUME_URL = "resume_url"
        const val KEY_LECTURE_TIME = "lecture_time"
        const val KEY_AVATAR_URI = "avatar_uri"
        const val KEY_CAMERA_URI = "camera_uri"
    }
}
