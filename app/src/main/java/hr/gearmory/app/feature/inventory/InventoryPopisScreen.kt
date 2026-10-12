package hr.gearmory.app.feature.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.gearmory.app.feature.equipment.pieceConditions

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InventoryPopisScreen(viewModel: InventoryViewModel = viewModel()) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val editing = viewModel.entries.firstOrNull { it.id == editingId }
    val shown = popisSorted(viewModel.entries.toList())

    LaunchedEffect(Unit) {
        viewModel.reload()
    }

    if (editing != null) {
        PopisEdit(
            entry = editing,
            onCancel = { editingId = null },
            onSave = { code, condition, type, quantity, note, thickness, gearSize ->
                val saved = if (editing.isQuantity()) {
                    viewModel.updateQuantity(editing.id, type, quantity, note)
                } else {
                    viewModel.updatePiece(editing.id, code, condition, thickness, gearSize)
                }
                if (saved) editingId = null
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = { confirmClear = true },
                enabled = shown.isNotEmpty() && viewModel.notice != CsvReadError,
            ) {
                Text("Očisti sve")
            }
        }
        if (viewModel.notice != null) {
            Text(
                text = viewModel.notice.orEmpty(),
                color = if (viewModel.notice == CsvReadError || viewModel.notice == CsvWriteError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (shown.isEmpty()) {
            Text(
                text = "Nema unosa",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            PopisTable(
                entries = shown,
                onOpen = { editingId = it },
                onRemove = viewModel::remove,
            )
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Jeste li sigurni?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        viewModel.clearAll()
                    },
                ) {
                    Text("Da")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Ne")
                }
            },
        )
    }
}

@Composable
private fun PopisTable(
    entries: List<InventoryEntry>,
    onOpen: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
            PopisRow(
                type = "Vrsta",
                code = "Šifra",
                size = "Veličina",
                thickness = "Debljina",
                condition = "Stanje",
                quantity = "Količina",
                note = "Napomena",
                header = true,
                onOpen = {},
                onRemove = {},
            )
            }
            entries.forEach { entry ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PopisRow(
                type = entry.type,
                code = entry.code,
                size = if (entry.isQuantity()) "" else shownSize(entry.type, entry.code, entry.size),
                thickness = if (entry.type == "Odijelo") entry.thickness else "—",
                condition = entry.condition,
                quantity = entry.quantity,
                note = entry.note,
                header = false,
                onOpen = { onOpen(entry.id) },
                onRemove = { onRemove(entry.id) },
            )
        }
        }
    }
}

@Composable
private fun PopisRow(
    type: String,
    code: String,
    size: String,
    thickness: String,
    condition: String,
    quantity: String,
    note: String,
    header: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(start = 16.dp)
            .clickable(enabled = !header, onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PopisCell(type, Modifier.weight(1.3f), header)
        PopisCell(code, Modifier.weight(1f), header)
        PopisCell(size, Modifier.weight(0.8f), header)
        PopisCell(thickness, Modifier.weight(0.8f), header)
        PopisCell(condition, Modifier.weight(1.1f), header)
        PopisCell(quantity, Modifier.weight(0.8f), header)
        PopisCell(note, Modifier.weight(1.2f), header)
        if (header) {
            Spacer(Modifier.size(48.dp))
        } else {
            IconButton(onClick = onRemove, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Makni redak")
            }
        }
    }
}

@Composable
private fun PopisCell(text: String, modifier: Modifier, header: Boolean) {
    Text(
        text = text,
        modifier = modifier,
        fontWeight = if (header) FontWeight.SemiBold else FontWeight.Medium,
        color = if (header) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopisEdit(
    entry: InventoryEntry,
    onCancel: () -> Unit,
    onSave: (code: String, condition: String, type: String, quantity: String, note: String, thickness: String, gearSize: String) -> Unit,
) {
    var code by rememberSaveable(entry.id) { mutableStateOf(entry.code) }
    var condition by rememberSaveable(entry.id) { mutableStateOf(entry.condition) }
    var thickness by rememberSaveable(entry.id) { mutableStateOf(entry.thickness) }
    var gearSize by rememberSaveable(entry.id) { mutableStateOf(entry.size) }
    var type by rememberSaveable(entry.id) { mutableStateOf(entry.type) }
    var quantity by rememberSaveable(entry.id) { mutableStateOf(entry.quantity) }
    var note by rememberSaveable(entry.id) { mutableStateOf(entry.note) }
    val razno = entry.isQuantity() && entry.type !in quantityTypes
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
    )
    val fieldShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = if (razno) "Vrsta" else entry.type,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        if (razno) {
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            Spacer(Modifier.height(20.dp))
        }
        if (entry.isQuantity()) {
            Text(
                text = "Količina",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { char -> char.isDigit() }.take(9) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            if (razno) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Napomena",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    singleLine = true,
                    shape = fieldShape,
                    colors = fieldColors,
                )
            }
        } else {
            Text(
                text = "Šifra",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            if (entry.type == "Kompenzator") {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Veličina",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    inventorySizes(entry.type).forEach { option ->
                        Button(
                            onClick = { gearSize = option },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (option == gearSize) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                contentColor = if (option == gearSize) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        ) {
                            Text(option)
                        }
                    }
                }
            }
            if (entry.type == "Odijelo") {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Debljina",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suitThicknesses.forEach { option ->
                        Button(
                            onClick = { thickness = option },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (option == thickness) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                contentColor = if (option == thickness) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        ) {
                            Text(option)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Stanje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pieceConditions.forEach { option ->
                    Button(
                        onClick = { condition = option },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (option == condition) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            contentColor = if (option == condition) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                    ) {
                        Text(option)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Datum unosa",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = entry.enteredOn.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text("Odustani")
            }
            Button(
                onClick = {
                    onSave(
                        code.trim(),
                        condition,
                        type,
                        quantity,
                        note,
                        thickness,
                        if (entry.type == "Kompenzator") gearSize else "",
                    )
                },
                enabled = entry.isQuantity() || (
                    (entry.type != "Odijelo" || thickness in suitThicknesses) &&
                        (entry.type != "Kompenzator" || gearSize in inventorySizes(entry.type))
                    ),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Spremi", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
