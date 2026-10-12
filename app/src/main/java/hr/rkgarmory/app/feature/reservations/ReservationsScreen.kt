package hr.rkgarmory.app.feature.reservations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
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
import hr.rkgarmory.app.ui.SegmentedTabs
import hr.rkgarmory.app.ui.theme.StatusInStock
import hr.rkgarmory.app.ui.theme.StatusIssued
import hr.rkgarmory.app.ui.theme.StatusWrittenOff
import java.math.BigDecimal

private const val AllStates = "Sve"
private const val ReservationPageSize = 20

private val stateTabs = listOf(
    AllStates to "Sve",
    "0" to "Na čekanju",
    "1" to "Aktivno",
    "2" to "Vraćeno",
)

internal fun donationText(amount: Double?): String =
    amount?.let { BigDecimal.valueOf(it).stripTrailingZeros().toPlainString().replace('.', ',') }
        .orEmpty()

private fun Reservation.codes(): String = equipmentKeys
    .mapNotNull { key ->
        val piece = equipment[key] ?: return@mapNotNull null
        if (piece.code.isNullOrBlank()) null else pieceMark(key, piece)
    }
    .joinToString(", ")
    .ifEmpty { "—" }

private fun Reservation.donation(): String =
    donationAmount?.let { "${donationText(it)} €" } ?: "—"

@Composable
internal fun ReservationsScreen() {
    val viewModel: ReservationsViewModel = viewModel()
    val phone = LocalConfiguration.current.smallestScreenWidthDp < 600
    var query by rememberSaveable { mutableStateOf("") }
    var stateFilter by rememberSaveable { mutableStateOf(AllStates) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val editing = viewModel.reservations.firstOrNull { it.id == editingId }
    val text = query.trim()
    val visible = viewModel.reservations.filter { reservation ->
        val stateOk = stateFilter == AllStates || reservation.state.toString() == stateFilter
        val textOk = text.isEmpty() ||
            reservation.userName.contains(text, ignoreCase = true) ||
            reservation.excursion.orEmpty().contains(text, ignoreCase = true)
        stateOk && textOk
    }
    val pageCount = maxOf(1, (visible.size + ReservationPageSize - 1) / ReservationPageSize)
    val filterKey = "$stateFilter|$text"
    var lastFilter by rememberSaveable { mutableStateOf(filterKey) }
    if (lastFilter != filterKey) {
        lastFilter = filterKey
        page = 0
    }
    val safePage = page.coerceIn(0, pageCount - 1)
    val shown = visible.drop(safePage * ReservationPageSize).take(ReservationPageSize)

    LaunchedEffect(Unit) {
        viewModel.reload()
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { Text("Traži ime ili izlet") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.reload() },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Osvježi")
                }
            }
            Spacer(Modifier.height(10.dp))
            SegmentedTabs(
                options = stateTabs,
                selected = stateFilter,
                onSelect = { stateFilter = it },
            )
        }
        HorizontalDivider()
        if (shown.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nema rezervacija",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (phone) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shown, key = { it.id }) { reservation ->
                    ReservationCard(reservation, onOpen = { editingId = reservation.id })
                }
            }
        } else {
            ReservationTable(
                reservations = shown,
                onOpen = { editingId = it },
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(16.dp),
            )
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

@Composable
private fun ReservationCard(reservation: Reservation, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reservation.userName,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                ReservationPill(reservation)
            }
            val trip = reservation.excursion?.takeIf { it.isNotBlank() }
            if (trip != null) {
                Text(
                    text = trip,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row {
                Text(
                    text = reservation.codes(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (reservation.donationAmount != null) {
                    Text(
                        text = reservation.donation(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
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
    Surface(
        color = color,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = reservation.status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            softWrap = false,
            fontWeight = FontWeight.Medium,
        )
    }
}
