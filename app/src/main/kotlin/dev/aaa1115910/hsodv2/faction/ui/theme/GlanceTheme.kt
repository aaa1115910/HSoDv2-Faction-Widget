package dev.aaa1115910.hsodv2.faction.ui.theme

import androidx.compose.runtime.Composable
import androidx.glance.GlanceComposable

@Composable
fun HSoDv2FactionGlanceTheme(
    content: @GlanceComposable @Composable () -> Unit
) {
    androidx.glance.GlanceTheme {
        content()
    }
}