package ru.urfu.cryptoapp.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.model.DataError
import ru.urfu.cryptoapp.domain.model.toDataError
import ru.urfu.cryptoapp.domain.usecase.DownloadResumeUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveProfileUseCase

/** Документ, который экран должен один раз открыть во внешнем приложении. */
data class OpenDocumentUi(
    val uri: String,
    val mimeType: String,
)

data class ProfileUiState(
    val fullName: String? = null, // null — ещё не заполнено, экран покажет подсказку
    val position: String? = null,
    val lectureTime: String? = null,
    val avatarUri: String? = null,
    val hasResume: Boolean = false,
    val isDownloading: Boolean = false,
    val documentToOpen: OpenDocumentUi? = null,
    @StringRes val message: Int? = null,
)

class ProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val downloadResume: DownloadResumeUseCase,
) : ViewModel() {

    private data class DownloadState(
        val isDownloading: Boolean = false,
        val document: OpenDocumentUi? = null,
        @StringRes val message: Int? = null,
    )

    private val download = MutableStateFlow(DownloadState())

    val uiState: StateFlow<ProfileUiState> = combine(observeProfile(), download) { profile, download ->
        ProfileUiState(
            fullName = profile.fullName.ifBlank { null },
            position = profile.position.ifBlank { null },
            lectureTime = profile.lectureTime.ifBlank { null },
            avatarUri = profile.avatarUri,
            hasResume = profile.resumeUrl.isNotBlank(),
            isDownloading = download.isDownloading,
            documentToOpen = download.document,
            message = download.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun onResumeClick() {
        if (download.value.isDownloading) return
        download.value = DownloadState(isDownloading = true)
        viewModelScope.launch {
            download.value = try {
                val document = downloadResume(observeProfile().first().resumeUrl)
                DownloadState(document = OpenDocumentUi(document.uri, document.mimeType))
            } catch (e: Exception) {
                DownloadState(message = e.toDataError().resumeMessageRes())
            }
        }
    }

    fun onDocumentOpened() {
        download.update { it.copy(document = null) }
    }

    fun onNoDocumentViewer() {
        download.update { it.copy(document = null, message = R.string.resume_no_viewer) }
    }

    fun onMessageShown() {
        download.update { it.copy(message = null) }
    }
}

@StringRes
private fun DataError.resumeMessageRes(): Int = when (this) {
    DataError.NETWORK -> R.string.error_network
    DataError.NOT_FOUND -> R.string.resume_not_found
    DataError.FILE_TOO_LARGE -> R.string.resume_too_large
    DataError.SERVER, DataError.RATE_LIMIT, DataError.UNKNOWN -> R.string.resume_download_failed
}
