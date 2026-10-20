package ru.urfu.cryptoapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector
import ru.urfu.cryptoapp.R

object Routes {
    const val COIN_ID_ARG = "coinId"

    const val COINS_GRAPH = "coins"
    const val COIN_LIST = "coins/list"
    const val COIN_FILTERS = "coins/filters"
    const val COIN_DETAILS = "coins/details/{$COIN_ID_ARG}"
    fun coinDetails(coinId: String) = "coins/details/$coinId"

    const val FAVORITES_GRAPH = "favorites"
    const val FAVORITES_LIST = "favorites/list"
    const val FAVORITE_DETAILS = "favorites/details/{$COIN_ID_ARG}"
    fun favoriteDetails(coinId: String) = "favorites/details/$coinId"
}

/** Вкладки нижней навигации. route — вложенный граф вкладки, startRoute — его корневой экран. */
enum class BottomTab(
    val route: String,
    val startRoute: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Coins(Routes.COINS_GRAPH, Routes.COIN_LIST, R.string.tab_coins, Icons.AutoMirrored.Filled.List),
    Favorites(Routes.FAVORITES_GRAPH, Routes.FAVORITES_LIST, R.string.tab_favorites, Icons.Filled.Favorite),
}
