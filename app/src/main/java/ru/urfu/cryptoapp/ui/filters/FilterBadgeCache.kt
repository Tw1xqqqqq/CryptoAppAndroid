package ru.urfu.cryptoapp.ui.filters

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.urfu.cryptoapp.domain.model.CoinFilters

/**
 * Общий для экранов списка и фильтров кэш: нужно ли показывать бейдж «фильтры изменены».
 * Живёт только в памяти (singleton в Koin) — долгосрочно хранятся сами фильтры, а не бейдж.
 */
class FilterBadgeCache {

    private val _showBadge = MutableStateFlow(false)
    val showBadge: StateFlow<Boolean> = _showBadge.asStateFlow()

    fun update(filters: CoinFilters) {
        _showBadge.value = !filters.isDefault
    }
}
