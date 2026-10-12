package hr.gearmory.app.ui.shell

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.gearmory.app.feature.equipment.EquipmentScreen
import hr.gearmory.app.feature.home.HomeScreen
import hr.gearmory.app.feature.inventory.InventoryPopisScreen
import hr.gearmory.app.feature.inventory.InventoryScreen
import hr.gearmory.app.feature.issuance.IssuanceScreen
import hr.gearmory.app.feature.returns.ReturnsScreen
import kotlinx.coroutines.launch

@Composable
internal fun AppShell(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(Destination.Home) }
    var openReservationId by rememberSaveable { mutableStateOf<Long?>(null) }
    val configuration = LocalConfiguration.current
    val phone = configuration.smallestScreenWidthDp < 600
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val destinations = if (phone) phoneDestinations else Destination.entries
    LaunchedEffect(phone) {
        if (selected !in destinations) selected = Destination.Home
    }
    val select: (Destination) -> Unit = { destination ->
        openReservationId = null
        selected = destination
    }
    val openRequest: (Destination, Long) -> Unit = { destination, id ->
        openReservationId = id
        selected = destination
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        if (isPortrait || phone) {
            DrawerShell(
                selected = selected,
                destinations = destinations,
                darkTheme = darkTheme,
                onSelect = select,
                onToggleTheme = onToggleTheme,
                onLogout = onLogout,
                openReservationId = openReservationId,
                onOpenRequest = openRequest,
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    NavigationPanel(
                        selected = selected,
                        destinations = destinations,
                        darkTheme = darkTheme,
                        onSelect = select,
                        onToggleTheme = onToggleTheme,
                        onLogout = onLogout,
                        modifier = Modifier.width(220.dp),
                    )
                }
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                ShellContent(
                    selected = selected,
                    openReservationId = openReservationId,
                    onOpenRequest = openRequest,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DrawerShell(
    selected: Destination,
    destinations: List<Destination>,
    darkTheme: Boolean,
    onSelect: (Destination) -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    openReservationId: Long?,
    onOpenRequest: (Destination, Long) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                NavigationPanel(
                    selected = selected,
                    destinations = destinations,
                    darkTheme = darkTheme,
                    onSelect = { destination ->
                        onSelect(destination)
                        scope.launch { drawerState.close() }
                    },
                    onToggleTheme = onToggleTheme,
                    onLogout = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Menu,
                            contentDescription = "Otvori izbornik",
                        )
                    }
                    Text(
                        text = selected.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            HorizontalDivider()
            ShellContent(
                selected = selected,
                openReservationId = openReservationId,
                onOpenRequest = onOpenRequest,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ShellContent(
    selected: Destination,
    openReservationId: Long?,
    onOpenRequest: (Destination, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        when (selected) {
            Destination.Home -> HomeScreen(
                onOpenIssuance = { onOpenRequest(Destination.Issuance, it) },
                onOpenReturns = { onOpenRequest(Destination.Returns, it) },
            )
            Destination.Issuance -> IssuanceScreen(openId = openReservationId)
            Destination.Returns -> ReturnsScreen(openId = openReservationId)
            Destination.Equipment -> EquipmentScreen()
            Destination.Inventory -> InventoryScreen()
            Destination.InventoryPopis -> InventoryPopisScreen()
        }
    }
}

private val phoneDestinations = listOf(
    Destination.Home,
    Destination.Issuance,
    Destination.Returns,
)
