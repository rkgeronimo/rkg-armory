package hr.rkgarmory.app.feature.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.remember
import androidx.compose.material.icons.rounded.Search
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.rkgarmory.app.ui.SegmentedTabs
import hr.rkgarmory.app.ui.theme.StatusBroken
import hr.rkgarmory.app.ui.theme.StatusInStock
import hr.rkgarmory.app.ui.theme.StatusIssued
import hr.rkgarmory.app.ui.theme.StatusLost
import hr.rkgarmory.app.ui.theme.StatusWrittenOff

private const val EquipmentPageSize = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EquipmentScreen() {
    val viewModel: EquipmentViewModel = viewModel()
    val phone = LocalConfiguration.current.smallestScreenWidthDp < 600
    var query by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf("Sve") }
    var typeFilter by rememberSaveable { mutableStateOf("Sve") }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val editing = viewModel.pieces.firstOrNull { it.id == editingId }
    val visible = viewModel.pieces.filter { piece ->
        val statusOk = statusFilter == "Sve" || piece.status == statusFilter
        val typeOk = typeFilter == "Sve" || piece.apiType == typeFilter
        val text = query.trim()
        val textOk = text.isEmpty() ||
            piece.code.contains(text, ignoreCase = true) ||
            piece.type.contains(text, ignoreCase = true) ||
            piece.holder.orEmpty().contains(text, ignoreCase = true)
        statusOk && typeOk && textOk
    }
    val pageAll = statusFilter == "Sve" && typeFilter == "Sve"
    val pageCount = if (!pageAll || visible.isEmpty()) {
        1
    } else {
        (visible.size + EquipmentPageSize - 1) / EquipmentPageSize
    }
    val filterKey = "$statusFilter|$typeFilter|${query.trim()}"
    var lastFilter by rememberSaveable { mutableStateOf(filterKey) }
    if (lastFilter != filterKey) {
        lastFilter = filterKey
        page = 0
    }
    val safePage = page.coerceIn(0, pageCount - 1)
    val shown = if (pageAll) {
        visible.drop(safePage * EquipmentPageSize).take(EquipmentPageSize)
    } else {
        visible
    }

    LaunchedEffect(Unit) {
        viewModel.reload()
    }
    LaunchedEffect(editingId) {
        if (editingId != null) viewModel.notice = null
    }
    LaunchedEffect(creating) {
        if (creating) viewModel.notice = null
    }

    if (creating) {
        EquipmentCreateForm(
            saving = viewModel.saving,
            notice = viewModel.notice,
            onSave = { code, type, size, note ->
                viewModel.create(code, type, size, note) { creating = false }
            },
            onCancel = { creating = false },
        )
        return
    }

    if (editing != null) {
        EquipmentForm(
            piece = editing,
            saving = viewModel.saving,
            notice = viewModel.notice,
            onSave = { state, note ->
                viewModel.save(editing, state, note) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { Text("Traži šifru, vrstu ili ime") },
                    leadingIcon = { IconSearch() },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                RefreshButton(
                    refreshing = viewModel.refreshing,
                    onClick = { viewModel.reload() },
                )
                AddButton(phone = phone, onClick = { creating = true })
            }
            Spacer(Modifier.height(10.dp))
            if (phone) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterSelect(
                        label = "Status",
                        options = (listOf("Sve") + inventoryStatuses).map { it to it },
                        selected = statusFilter,
                        onSelect = { statusFilter = it },
                        modifier = Modifier.weight(1f),
                    )
                    FilterSelect(
                        label = "Vrsta",
                        options = listOf("Sve" to "Sve") + inventoryTypes,
                        selected = typeFilter,
                        onSelect = { typeFilter = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                SegmentedTabs(
                    options = (listOf("Sve") + inventoryStatuses).map { it to it },
                    selected = statusFilter,
                    onSelect = { statusFilter = it },
                )
                Spacer(Modifier.height(8.dp))
                SegmentedTabs(
                    options = listOf("Sve" to "Sve") + inventoryTypes.map { (id, label) ->
                        id to typeTabLabel(label)
                    },
                    selected = typeFilter,
                    onSelect = { typeFilter = it },
                )
            }
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
                    text = "Nema komada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val listModifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
            if (phone) {
                PieceCards(
                    pieces = shown,
                    onOpen = { editingId = it },
                    modifier = listModifier,
                )
            } else {
                PieceTable(
                    pieces = shown,
                    onOpen = { editingId = it },
                    modifier = listModifier,
                )
            }
            if (pageAll && pageCount > 1) {
                PageBar(
                    page = safePage,
                    pageCount = pageCount,
                    onPage = { page = it },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSelect(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == selected }?.second.orEmpty(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun RefreshButton(refreshing: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !refreshing,
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        if (refreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(Icons.Rounded.Refresh, contentDescription = "Osvježi")
        }
    }
}

@Composable
private fun AddButton(phone: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = if (phone) Modifier.size(56.dp) else Modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = if (phone) PaddingValues(0.dp) else ButtonDefaults.ContentPadding,
    ) {
        if (phone) {
            Icon(Icons.Rounded.Add, contentDescription = "Dodaj opremu")
        } else {
            Text("Dodaj opremu", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun IconSearch() {
    androidx.compose.material3.Icon(
        Icons.Rounded.Search,
        contentDescription = null,
    )
}

@Composable
private fun PieceTable(
    pieces: List<Piece>,
    onOpen: (String) -> Unit,
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
                code = "Šifra",
                type = "Vrsta",
                size = "Veličina",
                status = "Status",
                holder = "Zadužio/la",
                issuedOn = "Datum izdavanja",
                note = "Napomena",
                header = true,
                onOpen = {},
            )
            }
            pieces.forEach { piece ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TableRow(
                code = piece.code,
                type = piece.type,
                size = piece.size.ifBlank { pieceSize(piece.type, piece.code) },
                status = piece.status,
                holder = piece.holder.orEmpty(),
                issuedOn = piece.issuedOn.orEmpty(),
                note = piece.note,
                header = false,
                showPill = true,
                onOpen = { onOpen(piece.id) },
            )
        }
        }
    }
}

@Composable
private fun PieceCards(
    pieces: List<Piece>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        pieces.forEach { piece ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .clickable { onOpen(piece.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CardLabel("Šifra")
                        CardValue(piece.code)
                        Text(
                            text = piece.type,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        StatusPill(piece.status)
                    }
                    Row {
                        CardLabel("Veličina")
                        CardValue(piece.size.ifBlank { pieceSize(piece.type, piece.code) })
                    }
                    if (piece.status == StatusIssuedLabel) {
                        CardValue(
                            listOf(piece.holder.orEmpty(), piece.issuedOn.orEmpty())
                                .filter { it.isNotBlank() }
                                .joinToString(" · "),
                        )
                    }
                    if (piece.note.isNotBlank()) {
                        Text(
                            text = piece.note,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(end = 8.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CardValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TableRow(
    code: String,
    type: String,
    size: String,
    status: String,
    holder: String,
    issuedOn: String,
    note: String,
    header: Boolean,
    onOpen: () -> Unit,
    showPill: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = !header, onClick = onOpen)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(code, modifier = Modifier.weight(0.9f), fontWeight = if (header) FontWeight.SemiBold else FontWeight.Medium)
        Text(type, modifier = Modifier.weight(1.2f), fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal)
        Text(
            size,
            modifier = Modifier
                .weight(1.15f)
                .padding(end = 20.dp),
            fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        Box(modifier = Modifier.weight(1.35f)) {
            if (showPill) StatusPill(status) else if (header) {
                Text("Status", fontWeight = FontWeight.SemiBold)
            }
        }
        Text(holder, modifier = Modifier.weight(1.3f), fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal)
        Text(issuedOn, modifier = Modifier.weight(1.1f), fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal)
        Text(
            note,
            modifier = Modifier.weight(1.6f),
            fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun PageBar(
    page: Int,
    pageCount: Int,
    onPage: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PageButton(
            label = "Prethodna",
            enabled = page > 0,
            onClick = { onPage(page - 1) },
        )
        Text(
            text = "${page + 1} / $pageCount",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.SemiBold,
        )
        PageButton(
            label = "Dalje",
            enabled = page < pageCount - 1,
            onClick = { onPage(page + 1) },
        )
    }
}

@Composable
private fun PageButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusPill(status: String) {
    val color = when (status) {
        StatusIssuedLabel -> StatusIssued
        "Neispravno" -> StatusBroken
        "Izgubljeno" -> StatusLost
        "Otpisano" -> StatusWrittenOff
        else -> StatusInStock
    }
    Surface(
        color = color,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            softWrap = false,
            fontWeight = FontWeight.Medium,
        )
    }
}
