package hr.rkgarmory.app.ui.shell

import androidx.compose.ui.graphics.vector.ImageVector
import hr.rkgarmory.app.ui.BoxIcon
import hr.rkgarmory.app.ui.CalendarIcon
import hr.rkgarmory.app.ui.ChecklistIcon
import hr.rkgarmory.app.ui.HomeIcon
import hr.rkgarmory.app.ui.IssueIcon
import hr.rkgarmory.app.ui.ListIcon
import hr.rkgarmory.app.ui.ReturnIcon

internal enum class Destination(
    val title: String,
    val icon: ImageVector,
    /** Label in the phone bottom bar, where a fifth of the width must fit it. */
    val barLabel: String = title,
) {
    Home(
        title = "Početna",
        icon = HomeIcon,
    ),
    Issuance(
        title = "Izdavanje",
        icon = IssueIcon,
    ),
    Returns(
        title = "Razduživanje",
        icon = ReturnIcon,
        barLabel = "Razduži",
    ),
    Reservations(
        title = "Rezervacije",
        icon = CalendarIcon,
    ),
    Equipment(
        title = "Oprema",
        icon = BoxIcon,
    ),
    Inventory(
        title = "Inventura",
        icon = ChecklistIcon,
    ),
    InventoryPopis(
        title = "Popis inventure",
        icon = ListIcon,
    ),
}
