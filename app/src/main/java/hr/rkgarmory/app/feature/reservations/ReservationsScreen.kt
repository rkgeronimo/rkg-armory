package hr.rkgarmory.app.feature.reservations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.rkgarmory.app.feature.equipment.PageBar
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.equipmentKeys
import hr.rkgarmory.app.remote.pieceMark
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.ui.Avatar
import hr.rkgarmory.app.ui.FilterChips
import hr.rkgarmory.app.ui.SearchField
import hr.rkgarmory.app.ui.StatusChip
import hr.rkgarmory.app.ui.theme.StatusInStock
import hr.rkgarmory.app.ui.theme.StatusIssued
import hr.rkgarmory.app.ui.theme.StatusWrittenOff
import java.math.BigDecimal

private const val AllStates = "Sve"
private const val ReservationPageSize = 20
private const val NoTrip = "Bez izleta"

private val stateTabs = listOf(
    AllStates to "Sve",
    "0" to "Na čekanju",
    "1" to "Aktivno",
    "2" to "Vraćeno",
)

internal fun donationText(amount: Double?): String =
    amount?.let { BigDecimal.valueOf(it).stripTrailingZeros().toPlainString().replace('.', ',') }
        .orEmpty()

private fun Reservation.codeMarks(): List<String> = equipmentKeys
    .mapNotNull { key ->
        val piece = equipment[key] ?: return@mapNotNull null
        if (piece.code.isNullOrBlank()) null else pieceMark(key, piece)
    }

private fun Reservation.codes(): String = codeMarks().joinToString(", ").ifEmpty { "—" }

private fun Reservation.donation(): String =
    donationAmount?.let { "${donationText(it)} €" } ?: "—"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReservationsScreen() {
    val viewModel: ReservationsViewModel = viewModel()
    val phone = LocalConfiguration.current.smallestScreenWidthDp < 600
    var query by rememberSaveable { mutableStateOf("") }
    var stateFilter by rememberSaveable { mutableStateOf(AllStates) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    // Held here so the phone list keeps its place while a reservation is open.
    val phoneListState = rememberLazyListState()
    val editing = viewModel.reservations.firstOrNull { it.id == editingId }
    val text = query.trim()
    val matching = viewModel.reservations.filter { reservation ->
        text.isEmpty() ||
            reservation.userName.contains(text, ignoreCase = true) ||
            reservation.excursion.orEmpty().contains(text, ignoreCase = true) ||
            reservation.equipment.values.any { it.code.orEmpty().contains(text, ignoreCase = true) }
    }
    val stateCounts = stateTabs.associate { (id, _) -> id to 0 } +
        matching.groupingBy { it.state.toString() }.eachCount() +
        (AllStates to matching.size)
    val visible = matching.filter { reservation ->
        stateFilter == AllStates || reservation.state.toString() == stateFilter
    }
    // The phone list is grouped by excursion and scrolls whole; pages are for the tablet table.
    val pageCount = if (phone) 1 else maxOf(1, (visible.size + ReservationPageSize - 1) / ReservationPageSize)
    val filterKey = "$stateFilter|$text"
    var lastFilter by rememberSaveable { mutableStateOf(filterKey) }
    if (lastFilter != filterKey) {
        lastFilter = filterKey
        page = 0
    }
    val safePage = page.coerceIn(0, pageCount - 1)
    val shown = if (phone) visible else visible.drop(safePage * ReservationPageSize).take(ReservationPageSize)

    LaunchedEffect(StaffSession.cachedReservations) {
        viewModel.open()
    }
    LaunchedEffect(editingId) {
        if (editingId != null) viewModel.notice = null
    }

    if (editing != null) {
        ReservationForm(
            reservation = editing,
            saving = viewModel.saving,
            notice = viewModel.notice,
            onSave = { codes, returned, comment, donation, archive ->
                viewModel.save(editing.id, codes, returned, comment, donation, archive) {
                    editingId = null
                }
            },
            onCancel = { editingId = null },
        )
        return
    }

    if (viewModel.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.notice != null) {
            Text(
                text = viewModel.notice.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Traži ime, izlet ili šifru",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            FilterChips(
                options = stateTabs,
                selected = stateFilter,
                onSelect = { stateFilter = it },
                counts = stateCounts,
            )
        }
        HorizontalDivider()
        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = { viewModel.reload() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (shown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nema rezervacija",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (phone) {
                val groups = shown.groupBy { it.excursion?.takeIf(String::isNotBlank) ?: NoTrip }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = phoneListState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    groups.forEach { (trip, group) ->
                        stickyHeader(key = "header:$trip") {
                            GroupHeader(trip, group.size)
                        }
                        item(key = "group:$trip") {
                            ReservationGroup(group, onOpen = { editingId = it })
                        }
                    }
                }
            } else {
                ReservationTable(
                    reservations = shown,
                    onOpen = { editingId = it },
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        if (pageCount > 1) {
            PageBar(
                page = safePage,
                pageCount = pageCount,
                onPage = { page = it },
            )
        }
    }
}

/** Excursion name over its card; stays on top while its rows scroll. */
@Composable
private fun GroupHeader(trip: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = trip,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** One card per excursion, a row per reservation. */
@Composable
private fun ReservationGroup(reservations: List<Reservation>, onOpen: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        reservations.forEachIndexed { index, reservation ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ReservationRow(reservation, onOpen = { onOpen(reservation.id) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReservationRow(reservation: Reservation, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(reservation.userName)
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = reservation.userName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val marks = reservation.codeMarks()
            if (marks.isEmpty()) {
                Text(
                    text = "Bez opreme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    marks.forEach { CodeTag(it) }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ReservationPill(reservation)
            if (reservation.donationAmount != null) {
                Text(
                    text = reservation.donation(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** An issued piece's code as a small tag. */
@Composable
private fun CodeTag(mark: String) {
    Text(
        text = mark,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ReservationTable(
    reservations: List<Reservation>,
    onOpen: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                TableRow(
                    name = "Član",
                    trip = "Izlet",
                    codes = "Oprema",
                    donation = "Donacija",
                    weight = FontWeight.SemiBold,
                    status = { Text("Status", fontWeight = FontWeight.SemiBold) },
                )
            }
            LazyColumn {
                items(reservations, key = { it.id }) { reservation ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    TableRow(
                        name = reservation.userName,
                        trip = reservation.excursion.orEmpty(),
                        codes = reservation.codes(),
                        donation = reservation.donation(),
                        weight = FontWeight.Normal,
                        status = { ReservationPill(reservation) },
                        modifier = Modifier.clickable { onOpen(reservation.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TableRow(
    name: String,
    trip: String,
    codes: String,
    donation: String,
    weight: FontWeight,
    status: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(name, 1.5f, if (weight == FontWeight.Normal) FontWeight.Medium else weight)
        TableCell(trip, 1.6f, weight)
        Box(modifier = Modifier.weight(1.1f)) { status() }
        TableCell(codes, 2f, weight)
        TableCell(donation, 0.8f, weight)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight,
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(end = 12.dp),
        fontWeight = fontWeight,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun ReservationPill(reservation: Reservation) {
    val color = when (reservation.state) {
        1 -> StatusIssued
        2 -> StatusInStock
        else -> StatusWrittenOff
    }
    StatusChip(reservation.status, color)
}
