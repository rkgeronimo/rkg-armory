package hr.rkgarmory.app.feature.reservations

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hr.rkgarmory.app.feature.issuance.requestedMark
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.equipmentKeys
import hr.rkgarmory.app.remote.pieceTitle
import hr.rkgarmory.app.ui.Avatar
import hr.rkgarmory.app.ui.ChoiceChip
import hr.rkgarmory.app.ui.FormActions
import hr.rkgarmory.app.ui.FormCard
import hr.rkgarmory.app.ui.SectionLabel

private const val PieceOut = 1

private val returnStates = listOf(
    PieceOut to "Izdano",
    0 to "Vraćeno",
    3 to "Izgubljeno",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReservationForm(
    reservation: Reservation,
    saving: Boolean,
    notice: String?,
    onSave: (
        codeByKey: Map<String, String>,
        returnedByKey: Map<String, Int>,
        comment: String?,
        donation: String?,
        archive: Boolean,
    ) -> Unit,
    onCancel: () -> Unit,
) {
    val phone = LocalConfiguration.current.smallestScreenWidthDp < 600
    val keys = equipmentKeys.filter { it in reservation.equipment }
    val savedCode = { key: String -> reservation.equipment[key]?.code.orEmpty() }
    val savedReturned = { key: String -> reservation.equipment[key]?.returned ?: PieceOut }
    val savedDonation = donationText(reservation.donationAmount)
    val codes = remember(reservation.id) {
        mutableStateMapOf<String, String>().apply { keys.forEach { put(it, savedCode(it)) } }
    }
    val returned = remember(reservation.id) {
        mutableStateMapOf<String, Int>().apply { keys.forEach { put(it, savedReturned(it)) } }
    }
    var comment by remember(reservation.id) { mutableStateOf(reservation.comment.orEmpty()) }
    var donation by remember(reservation.id) { mutableStateOf(savedDonation) }
    var archived by remember(reservation.id) { mutableStateOf(false) }

    val newCodes = keys
        .filter { codes[it].orEmpty().isNotBlank() && codes[it] != savedCode(it) }
        .associateWith { codes[it].orEmpty() }
    val newReturned = keys
        .filter { savedCode(it).isNotBlank() && it !in newCodes && returned[it] != savedReturned(it) }
        .associateWith { returned[it] ?: PieceOut }
    val newComment = comment.trim()
        .takeIf { it.isNotEmpty() && it != reservation.comment.orEmpty().trim() }
    val newDonation = donation.trim().takeIf { it != savedDonation }
    val dirty = newCodes.isNotEmpty() || newReturned.isNotEmpty() ||
        newComment != null || newDonation != null || archived

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                    Spacer(Modifier.width(8.dp))
                    ReservationPill(reservation)
                }
            }
            FormCard {
                SectionLabel("Oprema")
                keys.forEachIndexed { index, key ->
                    val piece = reservation.equipment.getValue(key)
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    val showStates = savedCode(key).isNotBlank() && key !in newCodes
                    val codeField: @Composable () -> Unit = {
                        OutlinedTextField(
                            value = codes[key].orEmpty(),
                            onValueChange = { typed ->
                                codes[key] = if (key == "lead") {
                                    typed.filter { it.isDigit() }.take(9)
                                } else {
                                    typed.uppercase().filter { !it.isWhitespace() }.take(12)
                                }
                            },
                            enabled = !saving,
                            modifier = Modifier
                                .width(130.dp)
                                .height(56.dp),
                            placeholder = { Text(if (key == "lead") "Kg" else "Šifra") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = if (key == "lead") {
                                    KeyboardType.Number
                                } else {
                                    KeyboardType.Text
                                },
                            ),
                        )
                    }
                    val stateChips: @Composable () -> Unit = {
                        returnStates.forEach { (value, label) ->
                            ChoiceChip(
                                label = label,
                                selected = returned[key] == value,
                                onClick = { returned[key] = value },
                                enabled = !saving,
                            )
                        }
                    }
                    val title: @Composable (Modifier) -> Unit = { modifier ->
                        Text(
                            text = pieceTitle(key, piece),
                            modifier = modifier,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val requested: @Composable (Modifier) -> Unit = { modifier ->
                        Text(
                            text = "Traženo: ${requestedMark(piece)}",
                            modifier = modifier,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = if (phone) TextAlign.Start else TextAlign.End,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (phone) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                            ) {
                                title(Modifier)
                                requested(Modifier)
                            }
                            codeField()
                        } else {
                            title(Modifier.width(130.dp))
                            codeField()
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (showStates) stateChips()
                            }
                            requested(Modifier.widthIn(min = 110.dp))
                        }
                    }
                    if (phone && showStates) {
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            stateChips()
                        }
                    }
                }
            }
            FormCard {
                SectionLabel("Donacija")
                OutlinedTextField(
                    value = donation,
                    onValueChange = { typed ->
                        donation = typed
                            .filter { it.isDigit() || it == ',' || it == '.' }
                            .take(10)
                    },
                    enabled = !saving,
                    modifier = Modifier.width(200.dp),
                    singleLine = true,
                    suffix = { Text("€") },
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Spacer(Modifier.height(20.dp))
                SectionLabel("Napomena")
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .toggleable(
                            value = archived,
                            enabled = !saving,
                            role = Role.Switch,
                            onValueChange = { archived = it },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Arhiviraj",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Nakon spremanja nestaje s popisa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = archived,
                        onCheckedChange = null,
                        enabled = !saving,
                    )
                }
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
                onSave = { onSave(newCodes, newReturned, newComment, newDonation, archived) },
            )
        }
    }
}
