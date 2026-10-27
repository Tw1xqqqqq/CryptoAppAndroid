# CryptoApp — Практика 3 «Список с деталями»

Предметная область: **CoinGecko** (вариант 15). Данные пока моковые, в формате ответов API.

## Стек
- Jetpack Compose, Single Activity (`MainActivity` + `NavHost`)
- Навигация: `navigation-compose` (вложенный граф для вкладки «Монеты»)
- Архитектура: **MVVM** — `ViewModel` + `StateFlow<UiState>`, репозиторий за интерфейсом
- ViewModel превращает доменные модели в UI-модели с уже отформатированными строками (цена, капитализация, проценты) — экран только отображает их
- Список: `LazyColumn` с подгрузкой страниц (`page` / `per_page`, как в `/coins/markets`)
- Детали: `ConstraintLayout` (compose) — барьеры, направляющая (guideline), `fillToConstraints`

## Структура
```
domain/model        CoinMarket (/coins/markets), CoinDetails (/coins/{id})
domain/repository   CoinRepository — интерфейс источника данных
data/mock           MockCoinData — 20 монет
data/repository     MockCoinRepository — задержка «сети» + пагинация
di                  AppContainer — ручной DI
ui/navigation       Routes, BottomTab, CryptoApp (Scaffold + NavigationBar + NavHost)
ui/coins/list       CoinListViewModel, CoinListScreen
ui/coins/details    CoinDetailsViewModel (coinId из SavedStateHandle), CoinDetailsScreen
ui/coins/model      UI-модели и CoinUiMapper — ViewModel отдаёт экрану готовые строки
ui/common           общие компоненты, форматирование чисел
```

## Что в списке и что в деталях
| Список (`/coins/markets`) | Детали (`/coins/{id}`) |
|---|---|
| ранг, иконка, название, тикер, цена, изменение за 24ч, капитализация | шапка с ценой и диапазоном за 24ч, сетка рыночных показателей, категории, описание, алгоритм, дата запуска, настроение сообщества, ссылки |

## Навигация
- Нижняя панель: «Монеты» / «Избранное» / «Настройки» (две последние — заглушки на следующие практики).
- Состояние вкладок сохраняется при переключении (`saveState` / `restoreState`).
- Повторное нажатие на активную вкладку возвращает к её корню.
- Список → детали: `coins/details/{coinId}`, назад — стрелка в TopAppBar или системная кнопка.

## Запуск
Открыть папку в Android Studio (Ladybug или новее), дождаться Gradle Sync, запустить `app`.
