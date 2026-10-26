package org.samvid.orchit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun OrchitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) OrchitDarkColorScheme else OrchitLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = OrchitTypography,
        content = content,
    )
}
