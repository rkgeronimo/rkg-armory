package hr.rkgarmory.app.ui

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Icon colour of the system bars. The app draws its own colours behind
 * them (edge to edge), so it must say whether what sits behind the status
 * bar and behind the navigation bar is dark.
 */
@Composable
internal fun SystemBarIcons(darkBehindStatusBar: Boolean, darkBehindNavigationBar: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkBehindStatusBar
            isAppearanceLightNavigationBars = !darkBehindNavigationBar
        }
        // Below API 29 enableEdgeToEdge paints a navigation bar scrim once, for the
        // system theme, and the system does not follow the icons. Keep it in step.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            window.navigationBarColor = if (darkBehindNavigationBar) DarkScrim else LightScrim
        }
    }
}

/** The scrims androidx.activity's enableEdgeToEdge uses. */
private val LightScrim = android.graphics.Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = android.graphics.Color.argb(0x80, 0x1B, 0x1B, 0x1B)
