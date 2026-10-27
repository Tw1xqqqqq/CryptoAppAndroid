package ru.urfu.cryptoapp.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.urfu.cryptoapp.ui.coins.details.CoinDetailsRoute
import ru.urfu.cryptoapp.ui.coins.list.CoinListRoute
import ru.urfu.cryptoapp.ui.favorites.FavoritesRoute
import ru.urfu.cryptoapp.ui.filters.FiltersRoute

@Composable
fun CryptoApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.COINS_GRAPH,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            // Вкладка «Монеты»: список -> детали, список -> фильтры
            navigation(startDestination = Routes.COIN_LIST, route = Routes.COINS_GRAPH) {
                composable(Routes.COIN_LIST) {
                    CoinListRoute(
                        onCoinClick = { coinId -> navController.navigate(Routes.coinDetails(coinId)) },
                        onFiltersClick = { navController.navigate(Routes.COIN_FILTERS) },
                    )
                }
                composable(Routes.COIN_FILTERS) {
                    FiltersRoute(onClose = { navController.popBackStack() })
                }
                coinDetails(Routes.COIN_DETAILS, navController)
            }
            // Вкладка «Избранное»: свой список -> детали, чтобы не переключаться на вкладку «Монеты»
            navigation(startDestination = Routes.FAVORITES_LIST, route = Routes.FAVORITES_GRAPH) {
                composable(Routes.FAVORITES_LIST) {
                    FavoritesRoute(
                        onCoinClick = { coinId -> navController.navigate(Routes.favoriteDetails(coinId)) },
                    )
                }
                coinDetails(Routes.FAVORITE_DETAILS, navController)
            }
        }
    }
}

private fun NavGraphBuilder.coinDetails(route: String, navController: NavHostController) {
    composable(
        route = route,
        arguments = listOf(navArgument(Routes.COIN_ID_ARG) { type = NavType.StringType }),
    ) {
        // coinId попадает в SavedStateHandle ViewModel'и экрана деталей
        CoinDetailsRoute(onBack = { navController.popBackStack() })
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        BottomTab.entries.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (selected) {
                        // Повторное нажатие на активную вкладку — возврат к её корню
                        navController.popBackStack(tab.startRoute, inclusive = false)
                    } else {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
            )
        }
    }
}
