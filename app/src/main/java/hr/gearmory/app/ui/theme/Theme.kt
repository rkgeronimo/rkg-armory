package hr.gearmory.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val StatusInStock = Color(0xFF2B7FD4)
val StatusIssued = Color(0xFFD46A52)
val StatusBroken = Color(0xFFBA1A1A)
val StatusLost = Color(0xFF7A3E12)
val StatusWrittenOff = Color(0xFF5C6B76)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A6BB5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EDF8),
    onPrimaryContainer = Color(0xFF0B3A5C),
    secondary = Color(0xFF0E4E78),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9EDF8),
    onSecondaryContainer = Color(0xFF0B3A5C),
    background = Color.White,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF3F6F8),
    onSurfaceVariant = Color(0xFF5C6B76),
    outline = Color(0xFFD5DEE5),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7EBAE3),
    onPrimary = Color(0xFF062536),
    primaryContainer = Color(0xFF134863),
    onPrimaryContainer = Color(0xFFD5EEF9),
    secondary = Color(0xFF6A9BB8),
    onSecondary = Color(0xFF062536),
    secondaryContainer = Color(0xFF134863),
    onSecondaryContainer = Color(0xFFD5EEF9),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFFCA5A5),
)

@Composable
fun RkgArmoryTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
