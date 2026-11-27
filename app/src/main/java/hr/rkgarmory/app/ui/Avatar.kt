package hr.rkgarmory.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val avatarColors = listOf(
    Color(0xFF2B7FD4),
    Color(0xFFD46A52),
    Color(0xFF2E9E6A),
    Color(0xFF7B5CC4),
    Color(0xFFC98A12),
)

/**
 * A member's initials in a tinted circle. The tint follows the name, so
 * the same member keeps the same colour on every screen. Text is mixed
 * towards `onSurface` like [StatusChip], to stay readable in both themes.
 * Hidden from TalkBack: the name is always shown next to it.
 */
@Composable
internal fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val color = avatarColors[name.hashCode().mod(avatarColors.size)]
    val initials = name.split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.18f), CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = lerp(color, MaterialTheme.colorScheme.onSurface, 0.4f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
