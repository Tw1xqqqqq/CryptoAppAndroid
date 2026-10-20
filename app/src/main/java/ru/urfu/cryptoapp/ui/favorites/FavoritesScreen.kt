package ru.urfu.cryptoapp.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.data.mock.MockCoinData
import ru.urfu.cryptoapp.ui.coins.list.CoinListItem
import ru.urfu.cryptoapp.ui.common.PlaceholderScreen
import ru.urfu.cryptoapp.ui.theme.CryptoAppTheme

@Composable
fun FavoritesRoute(
    onCoinClick: (String) -> Unit,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(state = state, onCoinClick = onCoinClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onCoinClick: (String) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_favorites)) }) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state is FavoritesUiState.Loading -> CircularProgressIndicator()
                state is FavoritesUiState.Content && state.coins.isEmpty() -> PlaceholderScreen(
                    icon = Icons.Filled.FavoriteBorder,
                    text = stringResource(R.string.favorites_empty),
                )
                state is FavoritesUiState.Content -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "hint") {
                        Text(
                            text = stringResource(R.string.favorites_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(items = state.coins, key = { it.id }) { coin ->
                        CoinListItem(coin = coin, onClick = { onCoinClick(coin.id) })
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    CryptoAppTheme(dynamicColor = false) {
        FavoritesScreen(
            state = FavoritesUiState.Content(MockCoinData.markets.take(3)),
            onCoinClick = {},
        )
    }
}
