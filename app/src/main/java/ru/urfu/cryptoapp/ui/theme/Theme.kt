package ru.urfu.cryptoapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val PriceUp = Color(0xFF16A34A)
val PriceDown = Color(0xFFDC2626)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8C5A00),
    secondary = Color(0xFF6F5B40),
    tertiary = Color(0xFF51643F),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF7B955),
    secondary = Color(0xFFDDC2A1),
    tertiary = Color(0xFFB7CEA0),
)

@Composable
fun CryptoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
