package hr.rkgarmory.app.feature.equipment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hr.rkgarmory.app.ui.Avatar
import hr.rkgarmory.app.ui.ChoiceChip
import hr.rkgarmory.app.ui.FormActions
import hr.rkgarmory.app.ui.FormCard
import hr.rkgarmory.app.ui.SectionLabel

/**
 * One piece: a header card (type tile, code, type and size, status, who
 * holds it), then its state and note, then cancel and save.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EquipmentForm(
    piece: Piece,
    saving: Boolean,
    notice: String?,
    onSave: (state: Int, note: String) -> Unit,
    onCancel: () -> Unit,
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
                    shape = RoundedCornerShape(14.dp),
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
        }
    }
}
