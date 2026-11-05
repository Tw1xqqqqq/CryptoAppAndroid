package ru.urfu.cryptoapp.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.ui.theme.CryptoAppTheme

@Composable
fun ProfileRoute(
    onEditClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.documentToOpen) {
        val document = state.documentToOpen ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(Uri.parse(document.uri), document.mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
            viewModel.onDocumentOpened()
        } catch (e: ActivityNotFoundException) {
            viewModel.onNoDocumentViewer()
        }
    }

    ProfileScreen(
        state = state,
        onEditClick = onEditClick,
        onResumeClick = viewModel::onResumeClick,
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onResumeClick: () -> Unit,
    onMessageShown: () -> Unit,
) {
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
                title = { Text(stringResource(R.string.tab_profile)) },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, stringResource(R.string.profile_edit))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfileAvatar(avatarUri = state.avatarUri, size = 140.dp)
            Text(
                text = state.fullName ?: stringResource(R.string.profile_name_empty),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            state.position?.let { position ->
                Text(position, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
            if (state.hasResume) {
                Button(onClick = onResumeClick, enabled = !state.isDownloading) {
                    if (state.isDownloading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.profile_resume))
                    }
                }
            }
        }
    }
}

/** Круглая аватарка; пока фото нет (или не открылось) — видна иконка человека. */
@Composable
fun ProfileAvatar(avatarUri: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(size / 2),
        )
        if (avatarUri != null) {
            AsyncImage(
                model = avatarUri,
                contentDescription = stringResource(R.string.profile_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    CryptoAppTheme(dynamicColor = false) {
        ProfileScreen(
            state = ProfileUiState(fullName = "Captain Smollett", position = "Старший разработчик", hasResume = true),
            onEditClick = {},
            onResumeClick = {},
            onMessageShown = {},
        )
    }
}
