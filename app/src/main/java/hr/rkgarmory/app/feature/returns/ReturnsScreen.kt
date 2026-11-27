package hr.rkgarmory.app.feature.returns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.rkgarmory.app.feature.reservations.donationText
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.ReservedPiece
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.remote.excursionsWhileOutText
import hr.rkgarmory.app.remote.openPieces
import hr.rkgarmory.app.remote.pieceTitle
import hr.rkgarmory.app.ui.EmptyState
import hr.rkgarmory.app.ui.FormActions
import hr.rkgarmory.app.ui.RequestGroups
import hr.rkgarmory.app.ui.ReturnIcon
import hr.rkgarmory.app.ui.SearchField

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
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ReservationHeader(selected)
                HeldPiecesCard(
                    reservationId = selected.id,
                    pieces = pieces,
                    marks = viewModel.marks,
                    onMark = viewModel::mark,
                    donation = donation,
                    onDonation = { donation = it },
                    enabled = !viewModel.saving,
                )
                Spacer(Modifier.height(16.dp))
                FormActions(
                    saving = viewModel.saving,
                    canSave = marked || newDonation != null,
                    onCancel = { close() },
                    onSave = {
                        viewModel.save(selected.id, newDonation) { stillOpen ->
                            if (!stillOpen) close()
                        }
                    },
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ReservationHeader(reservation: Reservation) {
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
    ) {
        Text(
            text = reservation.userName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val excursions = reservation.excursionsWhileOutText()
        if (excursions != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = excursions,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

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
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PieceColumns(
                        type = "Tip",
                        size = "Veličina",
                        code = "Šifra",
                        header = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(184.dp))
                }
            }
            pieces.forEach { (key, piece) ->
                val mark = marks["$reservationId:$key"]
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PieceColumns(
                        type = pieceTitle(key, piece),
                        size = piece.size?.takeIf { it.isNotBlank() } ?: "—",
                        code = piece.code?.takeIf { it.isNotBlank() } ?: "—",
                        header = false,
                        modifier = Modifier.weight(1f),
                    )
                    MarkBox(
                        label = "Vraćeno",
                        selected = mark == 0,
                        lost = false,
                        onClick = { onMark(reservationId, key, 0) },
                    )
                    MarkBox(
                        label = "Izgubljeno",
                        selected = mark == 3,
                        lost = true,
                        onClick = { onMark(reservationId, key, 3) },
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    onValueChange = { typed ->
                        onDonation(typed.filter { it.isDigit() || it == ',' || it == '.' }.take(10))
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .width(168.dp)
                        .height(56.dp),
                    singleLine = true,
                    suffix = { Text("€") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        }
    }
}

@Composable
private fun PieceColumns(
    type: String,
    size: String,
    code: String,
    header: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (header) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val style = if (header) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    Row(
        modifier = modifier
            .heightIn(min = 64.dp)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = type,
            modifier = Modifier.weight(1.4f),
            style = style,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = size,
            modifier = Modifier.weight(0.8f),
            style = style,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = code,
            modifier = Modifier.weight(0.9f),
            style = style,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun MarkBox(
    label: String,
    selected: Boolean,
    lost: Boolean,
    onClick: () -> Unit,
) {
    val color = if (lost) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier.width(88.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (selected) color else Color.Transparent,
                        shape = CircleShape,
                    )
                    .border(
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) color else MaterialTheme.colorScheme.outline,
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = if (lost) Icons.Rounded.Close else Icons.Rounded.Check,
                        contentDescription = label,
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
        Text(
            text = label,
            color = if (lost) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}
