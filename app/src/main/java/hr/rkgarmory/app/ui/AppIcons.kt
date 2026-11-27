package hr.rkgarmory.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Thin outline icons for navigation, summary cards and empty states, drawn
 * from the same paths as the approved mockup (24 grid, 1.8 stroke). `Icon`
 * tints the stroke.
 */
private fun outlineIcon(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { data ->
            addPath(
                pathData = addPathNodes(data),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

internal val HomeIcon = outlineIcon(
    "Home",
    "M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z",
)

internal val IssueIcon = outlineIcon(
    "Issue",
    "M4 8h15l-4-4",
    "M20 16H5l4 4",
)

internal val ReturnIcon = outlineIcon(
    "Return",
    "M9 14 4 9l5-5",
    "M4 9h10.5a5.5 5.5 0 0 1 0 11H11",
)

internal val CalendarIcon = outlineIcon(
    "Calendar",
    "M5 5h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z",
    "M3 10h18",
    "M8 3v4",
    "M16 3v4",
)

internal val BoxIcon = outlineIcon(
    "Box",
    "M3 7l9-4 9 4v10l-9 4-9-4z",
    "M3 7l9 4 9-4",
    "M12 11v10",
)

internal val ChecklistIcon = outlineIcon(
    "Checklist",
    "M6 4h12a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
    "m8 10 2 2 4-4",
    "M8 16h8",
)

internal val ListIcon = outlineIcon(
    "List",
    "M9 6h11",
    "M9 12h11",
    "M9 18h11",
    "M4 6h.01",
    "M4 12h.01",
    "M4 18h.01",
)
