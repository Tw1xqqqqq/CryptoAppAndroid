package ru.urfu.cryptoapp.data.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ru.urfu.cryptoapp.domain.model.CoinFilters
import ru.urfu.cryptoapp.domain.repository.FiltersRepository
import java.io.IOException

/** Фильтры хранятся в Preferences DataStore в виде имён enum-констант. */
class FiltersRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : FiltersRepository {

    override val filters: Flow<CoinFilters> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            CoinFilters(
                currency = prefs[CURRENCY].toEnumOr(CoinFilters.DEFAULT.currency),
                order = prefs[ORDER].toEnumOr(CoinFilters.DEFAULT.order),
                category = prefs[CATEGORY].toEnumOr(CoinFilters.DEFAULT.category),
            )
        }

    override suspend fun save(filters: CoinFilters) {
        dataStore.edit { prefs ->
            prefs[CURRENCY] = filters.currency.name
            prefs[ORDER] = filters.order.name
            prefs[CATEGORY] = filters.category.name
        }
    }

    private companion object {
        val CURRENCY = stringPreferencesKey("currency")
        val ORDER = stringPreferencesKey("order")
        val CATEGORY = stringPreferencesKey("category")
    }
}

/** Неизвестное значение (например, после переименования константы) — откат к значению по умолчанию. */
private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default
