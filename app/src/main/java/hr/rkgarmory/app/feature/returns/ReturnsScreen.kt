package hr.rkgarmory.app.feature.returns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.rkgarmory.app.feature.reservations.donationText
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.ReservedPiece
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.remote.excursionsOutLabel
import hr.rkgarmory.app.remote.openPieces
import hr.rkgarmory.app.remote.pieceTitle
import hr.rkgarmory.app.ui.Avatar
import hr.rkgarmory.app.ui.EmptyState
import hr.rkgarmory.app.ui.FormActions
import hr.rkgarmory.app.ui.FormCard
import hr.rkgarmory.app.ui.RequestGroups
import hr.rkgarmory.app.ui.ReturnIcon
import hr.rkgarmory.app.ui.SearchField
import hr.rkgarmory.app.ui.StatusChip
import hr.rkgarmory.app.ui.theme.StatusIssued

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReturnsScreen(openId: Long? = null) {
    var pickedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var opened by rememberSaveable { mutableStateOf(false) }
    var appliedOpenId by rememberSaveable { mutableStateOf<Long?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val viewModel: ReturnsViewModel = viewModel()
    val shown = viewModel.reservations.filter { it.openPieces().isNotEmpty() }
    val filtered = shown.filter {
        it.userName.contains(query, ignoreCase = true) ||
            it.excursion.orEmpty().contains(query, ignoreCase = true)
    }
    val selected = shown.firstOrNull { it.id == pickedId }
    val pieces = selected?.openPieces().orEmpty()
    val marked = pieces.any { viewModel.marks["${selected?.id}:${it.first}"] != null }
    val savedDonation = donationText(selected?.donationAmount)
    var donation by rememberSaveable(selected?.id, savedDonation) { mutableStateOf(savedDonation) }
    val newDonation = donation.trim().takeIf { it != savedDonation }

    LaunchedEffect(StaffSession.cachedActive) {
        viewModel.open()
    }
    LaunchedEffect(openId) {
        if (openId != null && appliedOpenId != openId) {
            appliedOpenId = openId
            viewModel.clearMarks()
            pickedId = openId
            opened = true
        }
    }

    fun pick(reservation: Reservation) {
        if (reservation.id != pickedId) viewModel.clearMarks()
        pickedId = reservation.id
    }

    fun close() {
        viewModel.clearMarks()
        pickedId = null
        opened = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }
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

        if (!opened || selected == null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Pretraži",
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
                PullToRefreshBox(
                    isRefreshing = viewModel.refreshing,
                    onRefresh = {
                        if (!viewModel.saving) {
                            viewModel.clearMarks()
                            pickedId = null
                            viewModel.reload()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                text = if (query.isBlank()) "Nema zahtjeva" else "Nema pronađenih",
                                icon = if (query.isBlank()) ReturnIcon else Icons.Rounded.SearchOff,
                            )
                        }
                    } else {
                        RequestGroups(
                            reservations = filtered,
                            action = "Razduži",
                            onOpen = { reservation ->
                                pick(reservation)
                                opened = true
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 720.dp)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ReservationHeader(selected)
                    HeldPiecesCard(
                        reservationId = selected.id,
                        pieces = pieces,
                        marks = viewModel.marks,
                        onMark = viewModel::mark,
                        donation = donation,
                        onDonation = { typed ->
                            donation = typed
                                .filter { it.isDigit() || it == ',' || it == '.' }
                                .take(10)
                        },
                        enabled = !viewModel.saving,
                    )
                    Spacer(Modifier.height(4.dp))
                    FormActions(
                        saving = viewModel.saving,
                        canSave = marked || newDonation != null,
                        onCancel = { close() },
                        onSave = {
                            viewModel.save(selected.id, newDonation) { stillOpen ->
                                if (!stillOpen) close()
                            }
                        },
                    )
                }
            }
        }
    }
}

/** Member, excursion and how many excursions the gear has been out for. */
@Composable
private fun ReservationHeader(reservation: Reservation) {
    FormCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(reservation.userName, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reservation.userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val trip = reservation.excursion?.takeIf { it.isNotBlank() }
                if (trip != null) {
                    Text(
                        text = trip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            reservation.excursionsOutLabel()?.let { label ->
                Spacer(Modifier.width(8.dp))
                StatusChip(label, StatusIssued)
            }
        }
    }
}

/**
 * The pieces still out as a compact table: one row per piece (name with
 * size, code, "Vraćeno" and "Izgubljeno" marks), the column names once on
 * top, the donation as the last row. Picking the marked one again clears it.
 */
@Composable
private fun HeldPiecesCard(
    reservationId: Long,
    pieces: List<Pair<String, ReservedPiece>>,
    marks: Map<String, Int>,
    onMark: (Long, String, Int) -> Unit,
    donation: String,
    onDonation: (String) -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ColumnTitle("Oprema", Modifier.weight(1.3f))
            ColumnTitle("Šifra", Modifier.weight(0.9f))
            ColumnTitle("Vraćeno", Modifier.width(MarkColumn), center = true)
            ColumnTitle(
                "Izgubljeno",
                Modifier.width(MarkColumn),
                center = true,
                color = MaterialTheme.colorScheme.error,
            )
        }
        pieces.forEach { (key, piece) ->
            val mark = marks["$reservationId:$key"]
            val title = pieceTitle(key, piece)
            val code = piece.code?.takeIf { it.isNotBlank() }
            val spoken = listOfNotNull(title, code).joinToString(" ")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .heightIn(min = 60.dp)
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    piece.size?.takeIf { it.isNotBlank() }?.let { size ->
                        Text(
                            text = "vel. $size",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box(modifier = Modifier.weight(0.9f)) {
                    Text(
                        text = code ?: "—",
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(6.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
                MarkCircle(
                    label = "Vraćeno",
                    piece = spoken,
                    selected = mark == 0,
                    lost = false,
                    enabled = enabled,
                    onClick = { onMark(reservationId, key, 0) },
                )
                MarkCircle(
                    label = "Izgubljeno",
                    piece = spoken,
                    selected = mark == 3,
                    lost = true,
                    enabled = enabled,
                    onClick = { onMark(reservationId, key, 3) },
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Donacija",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            OutlinedTextField(
                value = donation,
                onValueChange = onDonation,
                enabled = enabled,
                modifier = Modifier
                    .width(160.dp)
                    .height(56.dp),
                singleLine = true,
                suffix = { Text("€") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
    }
}

/** Width of the "Vraćeno" and "Izgubljeno" columns. */
private val MarkColumn = 68.dp

/**
 * A column name, on one line. With a large system font it shrinks to fit
 * its column instead of being cut ("Izgubljeno" in [MarkColumn]).
 */
@Composable
private fun ColumnTitle(
    text: String,
    modifier: Modifier = Modifier,
    center: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val style = MaterialTheme.typography.labelMedium
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color,
            fontWeight = FontWeight.SemiBold,
            textAlign = if (center) TextAlign.Center else TextAlign.Start,
        ),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 8.sp,
            maxFontSize = style.fontSize,
            stepSize = 0.5.sp,
        ),
    )
}

/**
 * A round mark in a table cell: empty outline, or filled with a check
 * (returned, `primary`) or a cross (lost, `error`). TalkBack reads [label]
 * with the [piece] it belongs to.
 */
@Composable
private fun MarkCircle(
    label: String,
    piece: String,
    selected: Boolean,
    lost: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = if (lost) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .width(MarkColumn)
            .height(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .toggleable(
                    value = selected,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                )
                .semantics { contentDescription = "$label: $piece" },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(if (selected) color else Color.Transparent, CircleShape)
                    .border(
                        1.dp,
                        if (selected) color else MaterialTheme.colorScheme.outline,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = if (lost) Icons.Rounded.Close else Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (lost) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
