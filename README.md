# CryptoApp — Практика 6 «Профиль»

Предметная область: **CoinGecko** (вариант 15). Данные загружаются из [CoinGecko API v3](https://docs.coingecko.com/reference/introduction),
фильтры и профиль хранятся в **DataStore**, избранное — в **Room**.

## Практика 6
Вкладка «Профиль» из двух экранов: просмотр и редактирование (карандаш в шапке, «Готово» — галочка).

### Хранение
| Данные | Где | Почему |
|---|---|---|
| ФИО, должность, URI аватарки, ссылка на резюме | типизированный DataStore, `profile.json` | одна запись без выборок: Room избыточна, в Preferences пришлось бы вести ключ на каждое поле; объект записывается целиком и атомарно |
| Файл аватарки | `filesDir/avatars` | выданный пикером доступ к исходному фото может истечь, поэтому хранится своя копия; лишние копии удаляются |
| Скачанное резюме | `cacheDir/documents` | файл можно скачать заново, система может очистить кэш при нехватке места |

### Работа без разрешений
В манифесте нет `CAMERA`, `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE` и `WRITE_EXTERNAL_STORAGE`, диалог разрешений не показывается:
- **Галерея** — Photo Picker (`PickVisualMedia`): фото выбирает система, приложение получает доступ только к выбранному файлу.
  На Android 13+ пикер встроен в систему (на части устройств — с Android 11), на более старых его доставляет Google Play, иначе используется системный выбор файлов.
- **Камера** — системное приложение камеры (`TakePicture`), снимок записывается в файл, открытый ему через `FileProvider`.
  Разрешение не нужно, пока `CAMERA` не объявлено в манифесте.
- **Резюме** — скачивается в кэш приложения (OkHttp) и открывается через `ACTION_VIEW` + `FileProvider`.

### Детали
- Ссылку можно ввести без схемы (`www.hh.ru/cv.pdf` → `https://www.hh.ru/cv.pdf`); `http://` не принимается, Android блокирует незашифрованный трафик.
- Для скачивания отдельный HTTP-клиент: ключ CoinGecko уходит только в запросы к CoinGecko, а не на сайт с резюме. Размер файла ограничен 50 МБ.
- Черновик редактирования лежит в `SavedStateHandle`: он переживает выгрузку приложения, пока открыта камера.
- `ProfileViewModel` и `EditProfileViewModel` содержат логику и готовят данные для экранов; экраны только отображают состояние и запускают системные окна.

## Практика 5
### Задача 1. Фильтры (DataStore)
Экран «Фильтры» открывается шестерёнкой в списке монет. Три настройки, все уходят в запрос `/coins/markets`:
| Настройка | Параметр API | Значения |
|---|---|---|
| Валюта | `vs_currency` | USD / EUR / RUB |
| Сортировка | `order` | капитализация или объём, по убыванию / возрастанию |
| Категория | `category` | все, Layer 1, смарт-контракты, DeFi, мем-коины, стейблкоины, ИИ, игры |

«Готово» сохраняет настройки в Preferences DataStore (`FiltersRepositoryImpl`), список подписан на них через Flow
и перезагружается. После выгрузки приложения из памяти настройки читаются из DataStore при старте.

### Задача 2. Избранное (Room + KSP)
- ♡ в шапке экрана деталей добавляет / убирает монету.
- Вкладка «Избранное» читает данные только из Room (`FavoritesDao` → `Flow`), поэтому работает без интернета.
  Сохраняется снимок рыночных данных на момент добавления.

### Задача 3. Бейдж
Точка на кнопке фильтров, если настройки отличаются от значений по умолчанию.
`FilterBadgeCache` — общий для экрана списка и экрана фильтров класс-кэш в памяти (`StateFlow<Boolean>`),
создаётся Koin как `single` и внедряется в обе ViewModel.

## Стек
- Jetpack Compose, Single Activity, `navigation-compose`
- Сеть: **Retrofit 2.11** + **OkHttp 4.12** + **kotlinx.serialization**
- Асинхронность: корутины и Flow, запросы на `Dispatchers.IO`
- Отладка сети: **Chucker** (debug), `HttpLoggingInterceptor` в Logcat
- Архитектура: **Clean Architecture** + MVVM, DI — **Koin**
- Локальное хранение: **Room** (KSP), **Preferences DataStore**

## Запросы
| Экран | Запрос | Параметры |
|---|---|---|
| Список | `GET /coins/markets` | query: `vs_currency`, `order`, `category`, `per_page`, `page` (пагинация) |
| Детали | `GET /coins/{id}` | path: `id`; query: `localization`, `tickers`, `market_data`, ... |
| Детали (шапка) | `GET /coins/markets?ids={id}` | только если монеты нет в кэше |

## Слои
```
domain/model        CoinMarket, CoinDetails, CoinOverview, CoinFilters, DataError + DataException
domain/repository   CoinRepository, FiltersRepository, FavoritesRepository, ProfileRepository, AvatarRepository, DocumentRepository — интерфейсы
domain/usecase      монеты, фильтры, избранное, профиль
data/remote         CoinGeckoApi (Retrofit), DTO, ApiKeyInterceptor, safeApiCall, ApiParams
data/local/prefs    FiltersRepositoryImpl — Preferences DataStore
data/local/db       AppDatabase, FavoritesDao, FavoriteCoinEntity — Room
data/local/profile  ProfileRepositoryImpl — типизированный DataStore
data/mapper         DTO -> доменные модели (null-поля API, очистка HTML в описании)
data/cache          MemoryCache — кэш ответов сети в памяти с TTL
data/repository     CoinRepositoryImpl, FavoritesRepositoryImpl, AvatarRepositoryImpl, DocumentRepositoryImpl
di                  модули Koin, запускаются в CryptoApplication
ui/coins            список и детали
ui/coins/model      UI-модели и CoinUiMapper: ViewModel отдаёт экрану уже отформатированные строки
ui/filters          экран фильтров, FilterBadgeCache
ui/favorites        экран избранного
ui/profile          экраны профиля и редактирования
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
