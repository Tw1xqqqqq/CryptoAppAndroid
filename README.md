# CryptoApp — Практика 4 «Работа с сетью»

Предметная область: **CoinGecko** (вариант 15). Данные загружаются из [CoinGecko API v3](https://docs.coingecko.com/reference/introduction).

## Стек
- Jetpack Compose, Single Activity, `navigation-compose`
- Сеть: **Retrofit 2.11** + **OkHttp 4.12** + **kotlinx.serialization**
- Асинхронность: корутины и Flow, запросы на `Dispatchers.IO`
- Отладка сети: **Chucker** (debug), `HttpLoggingInterceptor` в Logcat
- Архитектура: **Clean Architecture** + MVVM

## Запросы
| Экран | Запрос | Параметры |
|---|---|---|
| Список | `GET /coins/markets` | query: `vs_currency`, `order`, `per_page`, `page` (пагинация) |
| Детали | `GET /coins/{id}` | path: `id`; query: `localization`, `tickers`, `market_data`, ... |
| Детали (шапка) | `GET /coins/markets?ids={id}` | только если монеты нет в кэше |

## Слои
```
domain/model        CoinMarket, CoinDetails, CoinOverview, DataError + DataException
domain/repository   CoinRepository — интерфейс
domain/usecase      GetCoinMarketsUseCase, GetCoinOverviewUseCase (combine двух Flow)
data/remote         CoinGeckoApi (Retrofit), DTO, ApiKeyInterceptor, safeApiCall
data/mapper         DTO -> доменные модели (null-поля API, очистка HTML в описании)
data/cache          MemoryCache — кэш в памяти с TTL
data/repository     CoinRepositoryImpl — сеть + кэш, withContext/flowOn(Dispatchers.IO)
di                  AppContainer (ручной DI), создаётся в CryptoApplication
ui/coins/model      UI-модели и CoinUiMapper — экран получает уже отформатированные строки
ui/...              ViewModel вызывают use case'ы и отдают StateFlow<UiState> с UI-моделями
```

## Загрузка и ошибки
- Состояния: загрузка первой страницы, подгрузка следующих, pull-to-refresh, контент, ошибка.
- Исключения Retrofit/OkHttp переводятся в `DataError`: `NETWORK`, `RATE_LIMIT` (429), `NOT_FOUND`, `SERVER`, `UNKNOWN`
  — для каждой своя подпись и кнопка «Повторить».
- Ошибка pull-to-refresh при уже загруженном списке показывается снекбаром.

## Бонус: кэширование через Flow
`observeCoinDetails` / `observeCoinMarket` работают по схеме «сначала кэш, потом сеть»:
кэш отдаётся сразу, при устаревании (TTL: цены — 1 мин, описание — 10 мин) — запрос в сеть.
Если сети нет, но данные уже были загружены — остаются они. Поэтому уже открытые монеты
открываются и без интернета, а новые — показывают ошибку.

## Chucker
В debug-сборке все запросы видны в Chucker: уведомление «Recording HTTP activity»
или долгое нажатие на иконку приложения → «Chucker».

## API-ключ (необязательно)
Без ключа API работает, но лимит запросов ниже. Демо-ключ можно указать в `local.properties`:
```
COINGECKO_API_KEY=CG-xxxxxxxx
```

## Запуск
Открыть папку в Android Studio (Ladybug или новее), дождаться Gradle Sync, запустить `app`.
