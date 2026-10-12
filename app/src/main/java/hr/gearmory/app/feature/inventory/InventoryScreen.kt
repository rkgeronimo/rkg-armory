package hr.gearmory.app.feature.inventory

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.gearmory.app.feature.equipment.codeLetter
import hr.gearmory.app.feature.equipment.typeTabLabel
import hr.gearmory.app.feature.equipment.typedNumberLimit
import hr.gearmory.app.feature.equipment.equipmentTypes
import hr.gearmory.app.ui.SegmentedTabs
import hr.gearmory.app.feature.equipment.pieceConditions
import hr.gearmory.app.feature.equipment.pieceSize

@Composable
internal fun InventoryScreen(viewModel: InventoryViewModel = viewModel()) {
    var type by rememberSaveable { mutableStateOf(equipmentTypes.first()) }
    var size by rememberSaveable { mutableStateOf("") }
    var regulator by rememberSaveable { mutableStateOf("") }
    var typed by rememberSaveable { mutableStateOf("") }
    var condition by rememberSaveable { mutableStateOf("Dobro") }
    var thickness by rememberSaveable { mutableStateOf("") }
    var drafts by remember { mutableStateOf(mapOf<String, String>()) }
    var raznoName by rememberSaveable { mutableStateOf("") }
    var raznoCount by rememberSaveable { mutableStateOf("") }
    var raznoNote by rememberSaveable { mutableStateOf("") }
    val sizes = inventorySizes(type)
    val brands = if (type == "Regulator") regulatorTypes else emptyList()
    val inCode = when (type) {
        "Regulator" -> regulator
        else -> size
    }
    val code = codeLetter(type) + inCode + typed
    val duplicate = code.isNotEmpty() && viewModel.entries.any { it.type == type && it.code == code }
    val thicknesses = if (type == "Odijelo") suitThicknesses else emptyList()
    val needsSize = type == "Peraje" || type == "Kompenzator" || type == "Rukavice"
    val canEnter = viewModel.notice != CsvReadError &&
        typed.isNotBlank() &&
        (!needsSize || size.isNotBlank()) &&
        (brands.isEmpty() || regulator.isNotBlank()) &&
        (thicknesses.isEmpty() || thickness in thicknesses) &&
        !duplicate
    val visible = viewModel.entries.take(5)
    val isPortrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    LaunchedEffect(Unit) {
        viewModel.reload { drafts = quantityTypes.associateWith { viewModel.quantityOf(it) } }
    }

    fun selectType(option: String) {
        type = option
        if (size !in inventorySizes(option)) size = ""
        if (option != "Regulator") regulator = ""
        if (option != "Odijelo") thickness = ""
        if (option == ChipOstalo) {
            drafts = quantityTypes.associateWith { viewModel.quantityOf(it) }
        }
    }

    fun enterPiece() {
        viewModel.addPiece(type, code, condition, thickness, if (type == "Kompenzator") size else "")
        if (viewModel.notice != null) return
        typed = ""
        size = ""
        regulator = ""
        thickness = ""
        condition = "Dobro"
    }

    fun enterQuantity(kind: String, raw: String, note: String = "") {
        viewModel.setQuantity(kind, raw, note)
        val name = if (kind in quantityTypes) kind else raznoNameOk(kind)
        if (name == null) return
        val saved = viewModel.quantityOf(name)
        if (saved.isEmpty()) return
        if (name in quantityTypes) {
            drafts = drafts + (name to saved)
        } else {
            raznoName = name
            raznoCount = saved
            raznoNote = viewModel.entries.firstOrNull { it.type == name }?.note.orEmpty()
        }
    }

    if (isPortrait) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TypeChips(type = type, onType = ::selectType)
            Spacer(Modifier.height(16.dp))
            InventoryNotice(viewModel.notice)
            InventoryEditor(
                type = type,
                sizes = sizes,
                size = size,
                onSize = { size = it },
                brands = brands,
                regulator = regulator,
                onRegulator = { regulator = it },
                condition = condition,
                onCondition = { condition = it },
                thicknesses = thicknesses,
                thickness = thickness,
                onThickness = { thickness = it },
                code = code,
                duplicate = duplicate,
                canEnter = canEnter,
                onKey = { key ->
                    typed = when (key) {
                        "⌫" -> typed.dropLast(1)
                        else -> (typed + key).filter { it.isDigit() || it == 'X' }.take(typedNumberLimit(type))
                    }
                },
                onEnter = ::enterPiece,
                drafts = drafts,
                onDraft = { kind, value ->
                    drafts = drafts + (kind to value.filter { it.isDigit() }.take(9))
                },
                onQuantity = { viewModel.setOstalo(drafts)
                    drafts = quantityTypes.associateWith { viewModel.quantityOf(it) }
                },
                raznoName = raznoName,
                onRaznoName = { raznoName = it },
                raznoCount = raznoCount,
                onRaznoCount = { raznoCount = it.filter { char -> char.isDigit() }.take(9) },
                raznoNote = raznoNote,
                onRaznoNote = { raznoNote = it },
                onRazno = { enterQuantity(raznoName, raznoCount, raznoNote) },
            )
            if (visible.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                EntryList(
                    entries = visible,
                    onRemove = viewModel::remove,
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        TypeChips(type = type, onType = ::selectType)
        Spacer(Modifier.height(12.dp))
        InventoryNotice(viewModel.notice)
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .verticalScroll(rememberScrollState()),
            ) {
                InventoryEditor(
                    type = type,
                    sizes = sizes,
                    size = size,
                    onSize = { size = it },
                    brands = brands,
                    regulator = regulator,
                    onRegulator = { regulator = it },
                    condition = condition,
                    onCondition = { condition = it },
                    thicknesses = thicknesses,
                    thickness = thickness,
                    onThickness = { thickness = it },
                    code = code,
                    duplicate = duplicate,
                    canEnter = canEnter,
                    onKey = { key ->
                        typed = when (key) {
                            "⌫" -> typed.dropLast(1)
                            else -> (typed + key).filter { it.isDigit() || it == 'X' }.take(typedNumberLimit(type))
                        }
                    },
                    onEnter = ::enterPiece,
                    drafts = drafts,
                    onDraft = { kind, value ->
                        drafts = drafts + (kind to value.filter { it.isDigit() }.take(9))
                    },
                    onQuantity = { viewModel.setOstalo(drafts)
                    drafts = quantityTypes.associateWith { viewModel.quantityOf(it) }
                },
                    raznoName = raznoName,
                    onRaznoName = { raznoName = it },
                    raznoCount = raznoCount,
                    onRaznoCount = { raznoCount = it.filter { char -> char.isDigit() }.take(9) },
                    raznoNote = raznoNote,
                    onRaznoNote = { raznoNote = it },
                    onRazno = { enterQuantity(raznoName, raznoCount, raznoNote) },
                )
            }
            Spacer(Modifier.size(16.dp))
            EntryList(
                entries = visible,
                onRemove = viewModel::remove,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            )
        }
    }
}

@Composable
private fun TypeChips(
    type: String,
    onType: (String) -> Unit,
) {
    SegmentedTabs(
        options = inventoryChips.map { it to typeTabLabel(it) },
        selected = type,
        onSelect = onType,
    )
}

@Composable
private fun EntryList(
    entries: List<InventoryEntry>,
    onRemove: (String) -> Unit,
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
                EntryRow(
                    type = "Vrsta",
                    code = "Šifra",
                    size = "Veličina",
                    thickness = "Debljina",
                    condition = "Stanje",
                    quantity = "Količina",
                    note = "Napomena",
                    header = true,
                    onRemove = {},
                )
            }
            if (entries.isEmpty()) {
                Text(
                    text = "Nema unosa",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            entries.forEach { entry ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                EntryRow(
                    type = entry.type,
                    code = if (entry.isQuantity()) "—" else entry.code,
                    size = if (entry.isQuantity()) {
                        "—"
                    } else {
                        shownSize(entry.type, entry.code, entry.size).ifBlank { "—" }
                    },
                    thickness = if (entry.type == "Odijelo") entry.thickness.ifBlank { "—" } else "—",
                    condition = if (entry.isQuantity()) "—" else entry.condition,
                    quantity = if (entry.isQuantity()) entry.quantity else "—",
                    note = if (entry.note.isEmpty()) "—" else entry.note,
                    header = false,
                    onRemove = { onRemove(entry.id) },
                )
            }
        }
    }
}

@Composable
private fun InventoryNotice(notice: String?) {
    if (notice == null) return
    Text(
        text = notice,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        color = if (notice == CsvReadError || notice == CsvWriteError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun ColumnScope.InventoryEditor(
    type: String,
    sizes: List<String>,
    size: String,
    onSize: (String) -> Unit,
    brands: List<Pair<String, String>>,
    regulator: String,
    onRegulator: (String) -> Unit,
    condition: String,
    onCondition: (String) -> Unit,
    thicknesses: List<String>,
    thickness: String,
    onThickness: (String) -> Unit,
    code: String,
    duplicate: Boolean,
    canEnter: Boolean,
    onKey: (String) -> Unit,
    onEnter: () -> Unit,
    drafts: Map<String, String>,
    onDraft: (String, String) -> Unit,
    onQuantity: () -> Unit,
    raznoName: String,
    onRaznoName: (String) -> Unit,
    raznoCount: String,
    onRaznoCount: (String) -> Unit,
    raznoNote: String,
    onRaznoNote: (String) -> Unit,
    onRazno: () -> Unit,
) {
    when (type) {
        ChipOstalo -> QuantityFields(
            rows = quantityTypes.associateWith { drafts[it].orEmpty() },
            onDraft = onDraft,
            onEnter = onQuantity,
        )
        ChipRazno -> RaznoFields(
            name = raznoName,
            count = raznoCount,
            note = raznoNote,
            onName = onRaznoName,
            onCount = onRaznoCount,
            onNote = onRaznoNote,
            onEnter = onRazno,
        )
        else -> EntryForm(
            sizes = sizes,
            size = size,
            onSize = onSize,
            brands = brands,
            regulator = regulator,
            onRegulator = onRegulator,
            condition = condition,
            onCondition = onCondition,
            thicknesses = thicknesses,
            thickness = thickness,
            onThickness = onThickness,
            code = code,
            duplicate = duplicate,
            canEnter = canEnter,
            onKey = onKey,
            onEnter = onEnter,
        )
    }
}

@Composable
private fun inventoryFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
)

@Composable
private fun QuantityFields(
    rows: Map<String, String>,
    onDraft: (String, String) -> Unit,
    onEnter: () -> Unit,
) {
    val ready = quantityTypes.any { normalizeQuantity(rows[it].orEmpty()) != null }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        quantityTypes.forEach { kind ->
            val value = rows[kind].orEmpty()
            Row(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = kind,
                    modifier = Modifier.width(120.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { onDraft(kind, it) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = inventoryFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
        Row(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.width(120.dp))
            Button(
                onClick = onEnter,
                enabled = ready,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            ) {
                Text("Spremi", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RaznoFields(
    name: String,
    count: String,
    note: String,
    onName: (String) -> Unit,
    onCount: (String) -> Unit,
    onNote: (String) -> Unit,
    onEnter: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onName,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Vrsta") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = inventoryFieldColors(),
        )
        OutlinedTextField(
            value = count,
            onValueChange = onCount,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Količina") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = inventoryFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OutlinedTextField(
            value = note,
            onValueChange = onNote,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Napomena") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = inventoryFieldColors(),
        )
        Button(
            onClick = onEnter,
            enabled = raznoNameOk(name) != null && normalizeQuantity(count) != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Unesi", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ColumnScope.EntryForm(
    sizes: List<String>,
    size: String,
    onSize: (String) -> Unit,
    brands: List<Pair<String, String>>,
    regulator: String,
    onRegulator: (String) -> Unit,
    condition: String,
    onCondition: (String) -> Unit,
    thicknesses: List<String>,
    thickness: String,
    onThickness: (String) -> Unit,
    code: String,
    duplicate: Boolean,
    canEnter: Boolean,
    onKey: (String) -> Unit,
    onEnter: () -> Unit,
) {
    Text(
        text = "Stanje",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        pieceConditions.forEach { option ->
            SelectChip(
                label = option,
                selected = option == condition,
                onClick = { onCondition(option) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    if (thicknesses.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Debljina",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            thicknesses.forEach { option ->
                SelectChip(
                    label = option,
                    selected = option == thickness,
                    onClick = { onThickness(option) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    if (brands.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Tip regulatora",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            brands.forEach { (label, prefix) ->
                SelectChip(
                    label = label,
                    selected = prefix == regulator,
                    onClick = { onRegulator(prefix) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    if (sizes.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Veličina",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sizes.forEach { option ->
                SelectChip(
                    label = option,
                    selected = option == size,
                    onClick = { onSize(option) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally),
    ) {
        OutlinedTextField(
            value = code,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Šifra") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = inventoryFieldColors(),
        )
        Spacer(Modifier.height(12.dp))
        NumberPad(onKey = onKey)
        Spacer(Modifier.height(12.dp))
        if (duplicate) {
            Text(
                text = "Već uneseno",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = onEnter,
            enabled = canEnter,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Unesi", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EntryRow(
    type: String,
    code: String,
    size: String,
    thickness: String,
    condition: String,
    quantity: String,
    note: String,
    header: Boolean,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val style = if (header) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge
        }
        val color = if (header) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        Text(
            text = type,
            modifier = Modifier.weight(1.25f),
            style = style,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
        Text(
            text = code,
            modifier = Modifier.weight(0.8f),
            style = style,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
        Text(
            text = size,
            modifier = Modifier.weight(0.95f),
            style = style,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
        Text(
            text = thickness,
            modifier = Modifier.weight(0.9f),
            style = style,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
        Text(
            text = condition,
            modifier = Modifier.weight(1.15f),
            style = style,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
        Text(
            text = quantity,
            modifier = Modifier.weight(1.05f),
            style = style,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
        Text(
            text = note,
            modifier = Modifier.weight(1.2f),
            style = style,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
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
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
    ) {
        Text(
            text = label,
            maxLines = 1,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun NumberPad(onKey: (String) -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("X", "0", "⌫"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { onKey(key) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (key == "X") {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        contentColor = if (key == "X") {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 1.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "⌫") {
                                Icon(
                                    Icons.Rounded.Backspace,
                                    contentDescription = "Obriši zadnji znak",
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
