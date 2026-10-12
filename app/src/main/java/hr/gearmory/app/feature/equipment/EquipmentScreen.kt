package hr.gearmory.app.feature.equipment

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
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.gearmory.app.ui.SegmentedTabs
import hr.gearmory.app.ui.theme.StatusBroken
import hr.gearmory.app.ui.theme.StatusInStock
import hr.gearmory.app.ui.theme.StatusIssued
import hr.gearmory.app.ui.theme.StatusLost
import hr.gearmory.app.ui.theme.StatusWrittenOff

private const val EquipmentPageSize = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EquipmentScreen() {
    val viewModel: EquipmentViewModel = viewModel()
    var query by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf("Sve") }
    var typeFilter by rememberSaveable { mutableStateOf("Sve") }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
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
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
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
            Spacer(Modifier.height(10.dp))
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
            PieceTable(
                pieces = shown,
                onOpen = { editingId = it },
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            )
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
private fun PageBar(
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
