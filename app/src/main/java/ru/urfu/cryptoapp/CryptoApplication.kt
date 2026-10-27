package ru.urfu.cryptoapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.urfu.cryptoapp.di.appModules

class CryptoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CryptoApplication)
            modules(appModules)
        }
    }
}
