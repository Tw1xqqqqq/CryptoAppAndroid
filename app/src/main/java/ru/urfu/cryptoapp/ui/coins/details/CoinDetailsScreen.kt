package ru.urfu.cryptoapp.ui.coins.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.data.mock.MockCoinData
import ru.urfu.cryptoapp.ui.coins.model.CoinDetailsUi
import ru.urfu.cryptoapp.ui.coins.model.CoinMarketUi
import ru.urfu.cryptoapp.ui.coins.model.CoinUiMapper
import ru.urfu.cryptoapp.ui.coins.model.LabeledValueUi
import ru.urfu.cryptoapp.ui.coins.model.LinkUi
import ru.urfu.cryptoapp.ui.coins.model.SentimentUi
import ru.urfu.cryptoapp.ui.common.CoinAvatar
import ru.urfu.cryptoapp.ui.common.ErrorState
import ru.urfu.cryptoapp.ui.common.PriceChangeChip
import ru.urfu.cryptoapp.ui.common.messageRes
import ru.urfu.cryptoapp.ui.theme.CryptoAppTheme
import ru.urfu.cryptoapp.ui.theme.PriceDown
import ru.urfu.cryptoapp.ui.theme.PriceUp

@Composable
fun CoinDetailsRoute(
    onBack: () -> Unit,
    viewModel: CoinDetailsViewModel = viewModel(factory = CoinDetailsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoinDetailsScreen(state = state, onBack = onBack, onRetry = viewModel::load)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailsScreen(
    state: CoinDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (state as? CoinDetailsUiState.Content)?.details?.name
                        ?: stringResource(R.string.coin_details_title)
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                CoinDetailsUiState.Loading -> CircularProgressIndicator()
                is CoinDetailsUiState.Error -> ErrorState(stringResource(state.error.messageRes()), onRetry)
                is CoinDetailsUiState.Content -> CoinDetailsContent(state.details)
            }
        }
    }
}

@Composable
private fun CoinDetailsContent(details: CoinDetailsUi) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card { CoinHeader(details) }

        details.market?.let { market ->
            Section(stringResource(R.string.section_market)) { MarketStatsGrid(market.stats) }
        }
        Section(stringResource(R.string.section_about)) { AboutBlock(details) }
        Section(stringResource(R.string.section_info)) { InfoBlock(details.info) }
        details.sentiment?.let { sentiment ->
            Section(stringResource(R.string.section_sentiment)) { SentimentBar(sentiment) }
        }
        if (details.links.isNotEmpty()) {
            Section(stringResource(R.string.section_links)) { LinksBlock(details.links) }
        }
    }
}

/**
 * Шапка на ConstraintLayout:
 * аватар слева, имя/тикер справа от него, бейдж ранга прижат к правому краю,
 * под барьером (самым нижним из аватара и тикера) — цена с плашкой изменения,
 * ниже — диапазон цены за 24ч на всю ширину.
 */
@Composable
private fun CoinHeader(details: CoinDetailsUi) {
    val market = details.market
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        val (avatar, name, symbol, rank, price, change, range) = createRefs()
        val headerBottom = createBottomBarrier(avatar, symbol)

        CoinAvatar(
            imageUrl = details.imageUrl,
            name = details.name,
            size = 56.dp,
            modifier = Modifier.constrainAs(avatar) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
        )
        Text(
            text = details.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(name) {
                top.linkTo(avatar.top)
                start.linkTo(avatar.end, margin = 12.dp)
                end.linkTo(rank.start, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
        )
        Text(
            text = details.symbol,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.constrainAs(symbol) {
                top.linkTo(name.bottom)
                start.linkTo(name.start)
            },
        )
        Text(
            text = details.rank,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .constrainAs(rank) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                },
        )

        if (market != null) {
            Text(
                text = market.price,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.constrainAs(price) {
                    top.linkTo(headerBottom, margin = 16.dp)
                    start.linkTo(parent.start)
                },
            )
            PriceChangeChip(
                change = market.change,
                modifier = Modifier.constrainAs(change) {
                    centerVerticallyTo(price)
                    start.linkTo(price.end, margin = 12.dp)
                },
            )
            PriceRange(
                market = market,
                modifier = Modifier.constrainAs(range) {
                    top.linkTo(price.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            )
        }
    }
}

@Composable
private fun PriceRange(market: CoinMarketUi, modifier: Modifier = Modifier) {
    Column(modifier) {
        LinearProgressIndicator(
            progress = { market.rangeFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
        )
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(market.low24h, style = MaterialTheme.typography.labelSmall)
            Text("24h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(market.high24h, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Сетка 2x3 на ConstraintLayout: колонки разделены вертикальной направляющей (guideline) на 50%,
 * строки выстраиваются друг под другом через барьеры, чтобы высота строки определялась
 * самой высокой ячейкой.
 */
@Composable
private fun MarketStatsGrid(cells: List<LabeledValueUi>) {
    ConstraintLayout(Modifier.fillMaxWidth()) {
        val middle = createGuidelineFromStart(0.5f)
        val refs = cells.map { createRef() }
        // Барьер под каждой строкой (кроме последней) — от него отсчитывается следующая строка
        val rowBottoms = refs.chunked(2).dropLast(1).map { rowRefs -> createBottomBarrier(*rowRefs.toTypedArray()) }

        cells.forEachIndexed { index, cell ->
            val row = index / 2
            val isLeft = index % 2 == 0
            val rowTop = if (row == 0) null else rowBottoms[row - 1]
            StatCell(
                label = stringResource(cell.label),
                value = cell.value,
                modifier = Modifier.constrainAs(refs[index]) {
                    if (rowTop == null) top.linkTo(parent.top) else top.linkTo(rowTop, margin = 12.dp)
                    if (isLeft) {
                        start.linkTo(parent.start)
                        end.linkTo(middle, margin = 8.dp)
                    } else {
                        start.linkTo(middle, margin = 8.dp)
                        end.linkTo(parent.end)
                    }
                    width = Dimension.fillToConstraints
                },
            )
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutBlock(details: CoinDetailsUi) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.animateContentSize()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            details.categories.forEach { category ->
                AssistChip(onClick = {}, label = { Text(category) })
            }
        }
        if (details.description.isNotBlank()) {
            Text(
                text = details.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(onClick = { expanded = !expanded }) {
                Text(stringResource(if (expanded) R.string.show_less else R.string.show_more))
            }
        }
    }
}

@Composable
private fun InfoBlock(rows: List<LabeledValueUi>) {
    Column {
        rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider()
            InfoRow(stringResource(row.label), row.value)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SentimentBar(sentiment: SentimentUi) {
    val up = sentiment.upFraction
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
        ) {
            if (up > 0f) Box(Modifier.weight(up).fillMaxSize().background(PriceUp))
            if (up < 1f) Box(Modifier.weight(1f - up).fillMaxSize().background(PriceDown))
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(sentiment.upText, color = PriceUp, style = MaterialTheme.typography.labelMedium)
            Text(sentiment.downText, color = PriceDown, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun LinksBlock(links: List<LinkUi>) {
    val uriHandler = LocalUriHandler.current
    Column {
        links.forEach { link ->
            ListItem(
                headlineContent = { Text(stringResource(link.title)) },
                supportingContent = { Text(link.url, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    // runCatching — чтобы не упасть, если на устройстве нет браузера
                    .clickable { runCatching { uriHandler.openUri(link.url) } },
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun CoinDetailsPreview() {
    CryptoAppTheme(dynamicColor = false) {
        CoinDetailsScreen(
            state = CoinDetailsUiState.Content(
                CoinUiMapper.toDetails(
                    details = MockCoinData.details("bitcoin")!!,
                    market = MockCoinData.markets.first(),
                ),
            ),
            onBack = {},
            onRetry = {},
        )
    }
}
