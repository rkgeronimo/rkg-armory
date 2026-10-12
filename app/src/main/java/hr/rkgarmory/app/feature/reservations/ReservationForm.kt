package hr.rkgarmory.app.feature.reservations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                    ) {
                        Text(
                            text = reservation.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val trip = reservation.excursion?.takeIf { it.isNotBlank() }
                        if (trip != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = trip,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    ReservationPill(reservation)
                }
                keys.forEach { key ->
                    val piece = reservation.equipment.getValue(key)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                                StateChip(
                                    label = label,
                                    selected = returned[key] == value,
                                    onClick = { if (!saving) returned[key] = value },
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
                                style = MaterialTheme.typography.bodyLarge,
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
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                stateChips()
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(modifier = Modifier.padding(16.dp)) {
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
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !saving) { archived = !archived }
                            .heightIn(min = 48.dp)
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        color = if (archived) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = CircleShape,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (archived) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (archived) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Arhivirano",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            if (notice != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = notice,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCancel,
                    enabled = !saving,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text("Odustani")
                }
                Button(
                    onClick = {
                        if (!saving && dirty) onSave(newCodes, newReturned, newComment, newDonation, archived)
                    },
                    enabled = dirty || saving,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Spremi", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun StateChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
    ) {
        Text(label)
    }
}
