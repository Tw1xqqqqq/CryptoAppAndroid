package ru.urfu.cryptoapp.ui.coins.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.data.mock.MockCoinData
import ru.urfu.cryptoapp.domain.model.CoinMarket
import ru.urfu.cryptoapp.ui.common.CoinAvatar
import ru.urfu.cryptoapp.ui.common.ErrorState
import ru.urfu.cryptoapp.ui.common.PriceChangeChip
import ru.urfu.cryptoapp.ui.common.formatCompactUsd
import ru.urfu.cryptoapp.ui.common.formatPrice
import ru.urfu.cryptoapp.ui.common.messageRes
import ru.urfu.cryptoapp.ui.theme.CryptoAppTheme

@Composable
fun CoinListRoute(
    onCoinClick: (String) -> Unit,
    viewModel: CoinListViewModel = viewModel(factory = CoinListViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoinListScreen(
        state = state,
        onCoinClick = onCoinClick,
        onLoadMore = viewModel::loadNextPage,
        onRefresh = viewModel::refresh,
        onRefreshErrorShown = viewModel::onRefreshErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListScreen(
    state: CoinListUiState,
    onCoinClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshErrorMessage = state.refreshError?.let { stringResource(it.messageRes()) }
    LaunchedEffect(refreshErrorMessage) {
        if (refreshErrorMessage != null) {
            snackbarHostState.showSnackbar(refreshErrorMessage)
            onRefreshErrorShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.coins_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading && state.coins.isEmpty() -> CircularProgressIndicator()
                state.error != null && state.coins.isEmpty() -> ErrorState(
                    message = stringResource(state.error.messageRes()),
                    onRetry = onLoadMore,
                )
                else -> CoinList(state, onCoinClick, onLoadMore)
            }
        }
    }
}

@Composable
private fun CoinList(
    state: CoinListUiState,
    onCoinClick: (String) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    PaginationEffect(listState, onLoadMore)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = state.coins, key = { it.id }) { coin ->
            CoinListItem(coin = coin, onClick = { onCoinClick(coin.id) })
        }
        if (state.isLoadingMore) {
            item(key = "loading_more") {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        if (state.error != null && state.coins.isNotEmpty()) {
            item(key = "error_more") {
                ErrorState(
                    message = stringResource(state.error.messageRes()),
                    onRetry = onLoadMore,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Когда до конца списка остаётся несколько элементов — просим следующую страницу. */
@Composable
private fun PaginationEffect(listState: LazyListState, onLoadMore: () -> Unit) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= layoutInfo.totalItemsCount - PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }
}

private const val PREFETCH_DISTANCE = 3

@Composable
fun CoinListItem(coin: CoinMarket, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = coin.marketCapRank.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp),
            )
            CoinAvatar(imageUrl = coin.imageUrl, name = coin.name)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${coin.symbol.uppercase()} · ${coin.marketCap.formatCompactUsd()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = coin.currentPrice.formatPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                PriceChangeChip(percent = coin.priceChangePercentage24h)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoinListScreenPreview() {
    CryptoAppTheme(dynamicColor = false) {
        CoinListScreen(
            state = CoinListUiState(coins = MockCoinData.markets.take(6)),
            onCoinClick = {},
            onLoadMore = {},
            onRefresh = {},
            onRefreshErrorShown = {},
        )
    }
}
