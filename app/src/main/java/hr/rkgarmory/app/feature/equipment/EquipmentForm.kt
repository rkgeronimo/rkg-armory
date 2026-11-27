package hr.rkgarmory.app.feature.equipment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.ui.Avatar
import hr.rkgarmory.app.ui.ChoiceChip
import hr.rkgarmory.app.ui.FormActions
import hr.rkgarmory.app.ui.FormCard
import hr.rkgarmory.app.ui.SectionLabel
import hr.rkgarmory.app.ui.StatusChip
import hr.rkgarmory.app.ui.theme.StatusInStock
import hr.rkgarmory.app.ui.theme.StatusIssued
import hr.rkgarmory.app.ui.theme.StatusLost
import hr.rkgarmory.app.ui.theme.StatusWrittenOff

/**
 * One piece: a header card (type tile, code, type and size, status, who
 * holds it), then its state and note, then cancel and save, then the
 * reservations it was on ([history], see [pieceHistory]; not for lead).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EquipmentForm(
    piece: Piece,
    saving: Boolean,
    notice: String?,
    onSave: (state: Int, note: String) -> Unit,
    onCancel: () -> Unit,
    history: List<Reservation>?,
) {
    var state by rememberSaveable(piece.id) { mutableIntStateOf(piece.state) }
    var note by rememberSaveable(piece.id) { mutableStateOf(piece.note) }
    val noteText = note.trim()
    val dirty = state != piece.state || noteText != piece.note
    val typeName = piece.type.ifBlank {
        inventoryTypes.firstOrNull { it.first == piece.apiType }?.second ?: piece.apiType
    }
    val size = piece.size.ifBlank { pieceSize(piece.type, piece.code) }
    val holder = listOf(piece.holder.orEmpty(), piece.issuedOn.orEmpty())
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .takeIf { piece.status == StatusIssuedLabel && it.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FormCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeTile(typeName, size = 52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = piece.code,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = listOfNotNull(typeName, size.takeIf { it.isNotBlank() }?.let { "vel. $it" })
                                .joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    StatusPill(piece.status)
                }
                if (holder != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        piece.holder?.takeIf { it.isNotBlank() }?.let { name ->
                            Avatar(name, size = 32.dp)
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            text = holder,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            FormCard {
                SectionLabel("Stanje")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    inventoryStates.forEach { (value, label) ->
                        ChoiceChip(
                            label = label,
                            selected = value == state,
                            onClick = { state = value },
                            enabled = !saving,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                SectionLabel("Napomena")
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
            if (notice != null) {
                Text(
                    text = notice,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(4.dp))
            FormActions(
                saving = saving,
                canSave = dirty,
                onCancel = onCancel,
                onSave = { onSave(state, noteText) },
            )
            if (piece.apiType != "lead") {
                Spacer(Modifier.height(8.dp))
                HistoryCard(piece, history)
            }
        }
    }
}

/**
 * Reservations that had [piece], newest first. Only what is loaded counts:
 * the backend leaves archived reservations out of the list. Lead has none:
 * a reservation holds its weight in kilograms, not a piece code.
 */
internal fun pieceHistory(piece: Piece, reservations: List<Reservation>): List<Reservation> {
    if (piece.code.isBlank() || piece.apiType == "lead") return emptyList()
    return reservations
        .filter { it.equipment[piece.apiType]?.code.equals(piece.code, ignoreCase = true) }
        .sortedByDescending { it.id }
}

/**
 * Who had the piece, one row per reservation: member, excursion and
 * reservation number, and what happened to this piece on it.
 * [history] null means the reservations are not loaded yet.
 */
@Composable
private fun HistoryCard(piece: Piece, history: List<Reservation>?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Povijest",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!history.isNullOrEmpty()) {
                Text(
                    text = "${history.size} ${reservationWord(history.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (history.isNullOrEmpty()) {
            Text(
                text = if (history == null) {
                    "Rezervacije još nisu učitane."
                } else {
                    "Nije bila ni na jednoj rezervaciji."
                },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Card
        }
        history.forEach { reservation ->
            val (label, color) = historyStatus(reservation, reservation.equipment[piece.apiType]?.returned)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                // TalkBack reads one row as one item: name, excursion, number, status.
                modifier = Modifier
                    .semantics(mergeDescendants = true) {}
                    .heightIn(min = 60.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(reservation.userName, size = 36.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reservation.userName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(
                            reservation.excursion?.takeIf { it.isNotBlank() },
                            "#${reservation.id}",
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusChip(label, color)
            }
        }
    }
}

/** What happened to the piece on one reservation, from its `returned` mark. */
private fun historyStatus(reservation: Reservation, returned: Int?): Pair<String, Color> = when {
    returned == 3 -> "Izgubljeno" to StatusLost
    returned == 0 -> "Vraćeno" to StatusInStock
    reservation.state == 1 -> "Izdano" to StatusIssued
    reservation.state == 2 -> "Vraćeno" to StatusInStock
    else -> reservation.status to StatusWrittenOff
}

/** "1 rezervacija", "3 rezervacije", "5 rezervacija". */
private fun reservationWord(count: Int): String =
    if (count % 10 in 2..4 && count % 100 !in 12..14) "rezervacije" else "rezervacija"
