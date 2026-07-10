package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    secondary = GreenSuccessDark,
    tertiary = OrangeWarningDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = RedErrorDark,
    onPrimary = BackgroundDark,
    onSecondary = BackgroundDark,
    onBackground = TextLight,
    onSurface = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = GreenSuccess,
    tertiary = OrangeWarning,
    background = BackgroundSlate,
    surface = SurfaceWhite,
    error = RedError,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun CollegeERPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand colors for standard professional ERP feel
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
