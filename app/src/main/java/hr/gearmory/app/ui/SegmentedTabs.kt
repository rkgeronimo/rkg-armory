package hr.gearmory.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun SegmentedTabs(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    LaunchedEffect(selected) {
        val index = options.indexOfFirst { it.first == selected }
        if (index < options.lastIndex - 1) return@LaunchedEffect
        withFrameNanos { }
        scroll.scrollTo(scroll.maxValue)
    }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    Surface(
        modifier = Modifier.widthIn(max = maxWidth),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .horizontalScroll(scroll),
        ) {
            options.forEachIndexed { index, (id, label) ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                }
                val on = id == selected
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(
                            if (on) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                        )
                        .clickable { onSelect(id) }
                        .padding(
                            start = if (index == 0) 22.dp else 16.dp,
                            end = if (index == options.lastIndex) 22.dp else 16.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (on) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = label,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        color = if (on) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    )
                }
            }
        }
    }
    }
}
