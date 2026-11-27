package hr.rkgarmory.app.ui.shell

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.rkgarmory.app.feature.equipment.EquipmentScreen
import hr.rkgarmory.app.feature.home.HomeScreen
import hr.rkgarmory.app.feature.inventory.InventoryPopisScreen
import hr.rkgarmory.app.feature.inventory.InventoryScreen
import hr.rkgarmory.app.feature.issuance.IssuanceScreen
import hr.rkgarmory.app.feature.reservations.ReservationsScreen
import hr.rkgarmory.app.feature.returns.ReturnsScreen
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
        if (phone && isPortrait) {
            BottomBarShell(
                selected = selected,
                destinations = destinations,
                darkTheme = darkTheme,
                onSelect = select,
                onToggleTheme = onToggleTheme,
                onLogout = onLogout,
                openReservationId = openReservationId,
                onOpenRequest = openRequest,
            )
        } else if (isPortrait || phone) {
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
                    onSelect = select,
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
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Phone in portrait: title bar on top, destinations in a bottom bar.
 * Theme and logout sit in the account menu at the top right. The bar
 * hides while the keyboard is up, so it does not ride on top of it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomBarShell(
    selected: Destination,
    destinations: List<Destination>,
    darkTheme: Boolean,
    onSelect: (Destination) -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    openReservationId: Long?,
    onOpenRequest: (Destination, Long) -> Unit,
) {
    val accountMenu = @Composable {
        AccountMenu(
            darkTheme = darkTheme,
            onToggleTheme = onToggleTheme,
            onLogout = onLogout,
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        // Home has its own header with the account menu in it.
        if (selected != Destination.Home) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(start = 20.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selected.title,
                        modifier = Modifier.weight(1f),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    accountMenu()
                }
            }
            HorizontalDivider()
        }
        ShellContent(
            selected = selected,
            openReservationId = openReservationId,
            onOpenRequest = onOpenRequest,
            onSelect = onSelect,
            modifier = Modifier.weight(1f),
            homeAccountMenu = accountMenu,
        )
        if (!WindowInsets.isImeVisible) {
            HorizontalDivider()
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == selected,
                        onClick = { onSelect(destination) },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = {
                            Text(
                                text = destination.barLabel,
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountMenu(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                Icons.Rounded.AccountCircle,
                contentDescription = "Račun",
                modifier = Modifier.size(28.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            DropdownMenuItem(
                text = { Text(if (darkTheme) "Svijetla tema" else "Tamna tema") },
                leadingIcon = {
                    Icon(
                        if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onToggleTheme()
                },
            )
            DropdownMenuItem(
                text = { Text("Odjava") },
                leadingIcon = { Icon(Icons.Rounded.Logout, contentDescription = null) },
                onClick = {
                    expanded = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun ShellContent(
    selected: Destination,
    openReservationId: Long?,
    onOpenRequest: (Destination, Long) -> Unit,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    homeAccountMenu: (@Composable () -> Unit)? = null,
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
                onShowIssuance = { onSelect(Destination.Issuance) },
                onShowReturns = { onSelect(Destination.Returns) },
                accountMenu = homeAccountMenu,
            )
            Destination.Issuance -> IssuanceScreen(openId = openReservationId)
            Destination.Returns -> ReturnsScreen(openId = openReservationId)
            Destination.Reservations -> ReservationsScreen()
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
    Destination.Reservations,
    Destination.Equipment,
)
