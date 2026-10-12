package hr.gearmory.app.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentReturn
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class Destination(
    val title: String,
    val icon: ImageVector,
) {
    Home(
        title = "Početna",
        icon = Icons.Rounded.Dashboard,
    ),
    Issuance(
        title = "Izdavanje",
        icon = Icons.Rounded.SwapHoriz,
    ),
    Returns(
        title = "Razduživanje",
        icon = Icons.Rounded.AssignmentReturn,
    ),
    Equipment(
        title = "Oprema",
        icon = Icons.Rounded.Inventory2,
    ),
    Inventory(
        title = "Inventura",
        icon = Icons.Rounded.FactCheck,
    ),
    InventoryPopis(
        title = "Popis inventure",
        icon = Icons.Rounded.ViewList,
    ),
}
