package ru.urfu.cryptoapp.ui.profile

import android.Manifest
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.model.LectureTime
import ru.urfu.cryptoapp.ui.theme.CryptoAppTheme

/**
 * Для фото разрешения не запрашиваются: Photo Picker и приложение камеры работают в своих процессах и
 * отдают результат через выданный на один файл доступ. Поэтому в манифесте нет ни CAMERA, ни READ_MEDIA_IMAGES.
 * Единственный диалог — разрешение на уведомления при сохранении (Android 13+).
 */
@Composable
fun EditProfileRoute(
    onClose: () -> Unit,
    viewModel: EditProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.save(notificationsAllowed = granted)
        }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onGalleryPhotoPicked(uri.toString())
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        viewModel.onCameraResult(success)
    }

    LaunchedEffect(state.cameraUriToLaunch) {
        val uri = state.cameraUriToLaunch ?: return@LaunchedEffect
        try {
            cameraLauncher.launch(Uri.parse(uri))
            viewModel.onCameraLaunched()
        } catch (e: ActivityNotFoundException) {
            viewModel.onCameraUnavailable()
        }
    }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            state.savedMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
            onClose()
        }
    }

    EditProfileScreen(
        state = state,
        onBack = onClose,
        onDone = {
            if (viewModel.validateForm()) {
                when {
                    NotificationManagerCompat.from(context).areNotificationsEnabled() ->
                        viewModel.save(notificationsAllowed = true)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else -> viewModel.save(notificationsAllowed = false) // отключены в настройках системы
                }
            }
        },
        onFullNameChange = viewModel::onFullNameChange,
        onPositionChange = viewModel::onPositionChange,
        onResumeUrlChange = viewModel::onResumeUrlChange,
        onLectureTimeChange = viewModel::onLectureTimeChange,
        onLectureTimePicked = viewModel::onLectureTimePicked,
        onGalleryClick = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onCameraClick = viewModel::onCameraChosen,
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onResumeUrlChange: (String) -> Unit,
    onLectureTimeChange: (String) -> Unit,
    onLectureTimePicked: (hour: Int, minute: Int) -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onMessageShown: () -> Unit,
) {
    var showSourceDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onDone, enabled = state.canSave) {
                        Icon(Icons.Filled.Check, stringResource(R.string.filters_apply))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditableAvatar(
                avatarUri = state.avatarUri,
                isBusy = state.isBusy,
                onClick = { showSourceDialog = true },
            )
            TextField(
                value = state.fullName,
                onValueChange = onFullNameChange,
                label = { Text(stringResource(R.string.profile_full_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = state.position,
                onValueChange = onPositionChange,
                label = { Text(stringResource(R.string.profile_position)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = state.resumeUrl,
                onValueChange = onResumeUrlChange,
                label = { Text(stringResource(R.string.profile_resume_url)) },
                singleLine = true,
                isError = state.isResumeUrlInvalid,
                supportingText = {
                    Text(
                        stringResource(
                            if (state.isResumeUrlInvalid) R.string.profile_resume_url_invalid else R.string.profile_resume_url_hint,
                        ),
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = state.lectureTime,
                onValueChange = onLectureTimeChange,
                label = { Text(stringResource(R.string.profile_lecture_time)) },
                singleLine = true,
                isError = state.isLoaded && state.isLectureTimeInvalid,
                supportingText = {
                    if (state.isLoaded && state.isLectureTimeInvalid) {
                        Text(stringResource(R.string.profile_lecture_time_invalid))
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_schedule),
                            contentDescription = stringResource(R.string.profile_pick_time),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showTimePicker) {
        LectureTimePickerDialog(
            initialTime = state.pickerTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                onLectureTimePicked(hour, minute)
            },
        )
    }

    if (showSourceDialog) {
        AvatarSourceDialog(
            onDismiss = { showSourceDialog = false },
            onGalleryClick = {
                showSourceDialog = false
                onGalleryClick()
            },
            onCameraClick = {
                showSourceDialog = false
                onCameraClick()
            },
        )
    }
}

@Composable
private fun EditableAvatar(avatarUri: String?, isBusy: Boolean, onClick: () -> Unit) {
    val description = stringResource(R.string.profile_change_photo)
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(enabled = !isBusy, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ProfileAvatar(avatarUri = avatarUri, size = 120.dp)
        if (isBusy) {
            CircularProgressIndicator()
        } else if (avatarUri == null) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .size(18.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LectureTimePickerDialog(
    initialTime: LectureTime,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun AvatarSourceDialog(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_change_photo)) },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.profile_source_gallery)) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable(onClick = onGalleryClick),
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.profile_source_camera)) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable(onClick = onCameraClick),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    CryptoAppTheme(dynamicColor = false) {
        EditProfileScreen(
            state = EditProfileUiState(
                isLoaded = true,
                fullName = "Captain Smollett",
                resumeUrl = "www.hh.ru/captain-smollett.pdf",
                lectureTime = "18:43",
            ),
            onBack = {},
            onDone = {},
            onFullNameChange = {},
            onPositionChange = {},
            onResumeUrlChange = {},
            onLectureTimeChange = {},
            onLectureTimePicked = { _, _ -> },
            onGalleryClick = {},
            onCameraClick = {},
            onMessageShown = {},
        )
    }
}
