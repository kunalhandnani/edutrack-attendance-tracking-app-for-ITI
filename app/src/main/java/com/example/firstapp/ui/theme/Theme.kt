package com.example.firstapp.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = CloudBlue,
    secondary = Mint,
    tertiary = Coral,
    background = SurfaceDark,
    surface = SurfaceDark,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    secondary = Mint,
    tertiary = Coral,
    background = SurfaceLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Ink,
    onTertiary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Slate
)

@Composable
fun FirstappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
