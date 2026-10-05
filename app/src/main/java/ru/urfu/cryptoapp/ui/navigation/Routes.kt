package ru.urfu.cryptoapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import ru.urfu.cryptoapp.R

object Routes {
    const val COINS_GRAPH = "coins"
    const val COIN_LIST = "coins/list"

    const val COIN_ID_ARG = "coinId"
    const val COIN_DETAILS = "coins/details/{$COIN_ID_ARG}"
    fun coinDetails(coinId: String) = "coins/details/$coinId"

    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
}

/** Вкладки нижней навигации. route — корень вкладки (граф или экран). */
enum class BottomTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Coins(Routes.COINS_GRAPH, R.string.tab_coins, Icons.AutoMirrored.Filled.List),
    Favorites(Routes.FAVORITES, R.string.tab_favorites, Icons.Filled.Favorite),
    Settings(Routes.SETTINGS, R.string.tab_settings, Icons.Filled.Settings),
}
