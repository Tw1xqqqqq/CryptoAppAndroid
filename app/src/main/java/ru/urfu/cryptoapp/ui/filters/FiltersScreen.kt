package ru.urfu.cryptoapp.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.model.CoinCategory
import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.model.SortOrder
import ru.urfu.cryptoapp.domain.model.VsCurrency
import ru.urfu.cryptoapp.ui.theme.CryptoAppTheme

@Composable
fun FiltersRoute(
    onClose: () -> Unit,
    viewModel: FiltersViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isApplied) {
        if (state.isApplied) onClose()
    }
    FiltersScreen(
        state = state,
        onBack = onClose,
        onCurrencySelected = viewModel::onCurrencySelected,
        onOrderSelected = viewModel::onOrderSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onReset = viewModel::reset,
        onApply = viewModel::apply,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    state: FiltersUiState,
    onBack: () -> Unit,
    onCurrencySelected: (VsCurrency) -> Unit,
    onOrderSelected: (SortOrder) -> Unit,
    onCategorySelected: (CoinCategory) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.filters_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = onReset, enabled = !state.filters.isDefault) {
                        Text(stringResource(R.string.filters_reset))
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onApply,
                enabled = state.isLoaded,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(stringResource(R.string.filters_apply))
            }
        },
    ) { padding ->
        if (!state.isLoaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            FilterSection(stringResource(R.string.filter_currency)) {
                CurrencySelector(state.filters.currency, onCurrencySelected)
            }
            FilterSection(stringResource(R.string.filter_order)) {
                OrderSelector(state.filters.order, onOrderSelected)
            }
            FilterSection(stringResource(R.string.filter_category)) {
                CategorySelector(state.filters.category, onCategorySelected)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelector(selected: VsCurrency, onSelected: (VsCurrency) -> Unit) {
    val options = VsCurrency.entries
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, currency ->
            SegmentedButton(
                selected = currency == selected,
                onClick = { onSelected(currency) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) {
                Text(stringResource(currency.labelRes))
            }
        }
    }
}

@Composable
private fun OrderSelector(selected: SortOrder, onSelected: (SortOrder) -> Unit) {
    Column(Modifier.selectableGroup()) {
        SortOrder.entries.forEach { order ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = order == selected,
                        onClick = { onSelected(order) },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 4.dp),
            ) {
                RadioButton(selected = order == selected, onClick = null)
                Text(stringResource(order.labelRes), modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(selected: CoinCategory, onSelected: (CoinCategory) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CoinCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelected(category) },
                label = { Text(stringResource(category.labelRes)) },
            )
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltersScreenPreview() {
    CryptoAppTheme(dynamicColor = false) {
        FiltersScreen(
            state = FiltersUiState(
                filters = CoinFilters(VsCurrency.EUR, SortOrder.VOLUME_DESC, CoinCategory.MEME),
                isLoaded = true,
            ),
            onBack = {},
            onCurrencySelected = {},
            onOrderSelected = {},
            onCategorySelected = {},
            onReset = {},
            onApply = {},
        )
    }
}
