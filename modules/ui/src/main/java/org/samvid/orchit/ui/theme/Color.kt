package org.samvid.orchit.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Orchit owl logo palette
val OwlGreen = Color(0xFF4CAF50)
val OwlGreenDark = Color(0xFF2E7D32)
val OwlPurple = Color(0xFF7E57C2)
val OwlPurpleDark = Color(0xFF5E35B1)
val Navy = Color(0xFF1A237E)
val NavyMedium = Color(0xFF283593)
val Silver = Color(0xFFC0C0C0)

val OrchitDarkColorScheme = darkColorScheme(
    primary = OwlGreen,
    secondary = OwlPurple,
    tertiary = Navy,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Silver,
    onSurface = Silver,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF444444),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF1E1E1E),
    inversePrimary = OwlGreenDark,
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
)

val OrchitLightColorScheme = lightColorScheme(
    primary = OwlGreenDark,
    secondary = OwlPurpleDark,
    tertiary = NavyMedium,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFFCCCCCC),
    inverseSurface = Color(0xFF2A2A2A),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFF81C784),
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)
