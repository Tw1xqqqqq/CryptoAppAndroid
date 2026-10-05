package ru.urfu.cryptoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.urfu.cryptoapp.ui.navigation.CryptoApp
import ru.urfu.cryptoapp.ui.theme.CryptoAppTheme

/** Single Activity: все экраны — compose-destinations внутри NavHost. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoAppTheme {
                CryptoApp()
            }
        }
    }
}
