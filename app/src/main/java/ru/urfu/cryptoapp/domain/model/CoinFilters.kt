package ru.urfu.cryptoapp.domain.model

/** Валюта, в которой показываются цены (`vs_currency`). */
enum class VsCurrency { USD, EUR, RUB }

/** Сортировка списка (`order`). */
enum class SortOrder { MARKET_CAP_DESC, MARKET_CAP_ASC, VOLUME_DESC, VOLUME_ASC }

/** Категория монет (`category`); [ALL] — без фильтра. */
enum class CoinCategory { ALL, LAYER_1, SMART_CONTRACT, DEFI, MEME, STABLECOINS, AI, GAMING }

/** Настройки списка монет, сохраняются между запусками приложения. */
data class CoinFilters(
    val currency: VsCurrency = VsCurrency.USD,
    val order: SortOrder = SortOrder.MARKET_CAP_DESC,
    val category: CoinCategory = CoinCategory.ALL,
) {
    val isDefault: Boolean get() = this == DEFAULT

    companion object {
        val DEFAULT = CoinFilters()
    }
}
