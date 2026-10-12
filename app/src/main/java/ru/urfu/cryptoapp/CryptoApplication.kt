package ru.urfu.cryptoapp

import android.app.Application
import ru.urfu.cryptoapp.di.AppContainer

class CryptoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
