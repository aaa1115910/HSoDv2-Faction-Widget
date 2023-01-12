package dev.aaa1115910.hsodv2.faction.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.glance.color.ColorProviders
import androidx.glance.color.dynamicThemeColorProviders

object GlanceTheme {
    val colors: ColorProviders
        @Composable
        @ReadOnlyComposable
        get() = LocalColorProviders.current
}

internal val LocalColorProviders = staticCompositionLocalOf { dynamicThemeColorProviders() }

@Composable
fun GlanceTheme(colors: ColorProviders = GlanceTheme.colors, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalColorProviders provides colors) {
        content()
    }
}