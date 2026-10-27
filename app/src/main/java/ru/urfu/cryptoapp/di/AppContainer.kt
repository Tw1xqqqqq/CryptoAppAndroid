package ru.urfu.cryptoapp.di

import ru.urfu.cryptoapp.data.repository.MockCoinRepository
import ru.urfu.cryptoapp.domain.repository.CoinRepository

/** Простейший ручной DI. Позже здесь MockCoinRepository заменится на сетевую реализацию. */
object AppContainer {
    val coinRepository: CoinRepository by lazy { MockCoinRepository() }
}
