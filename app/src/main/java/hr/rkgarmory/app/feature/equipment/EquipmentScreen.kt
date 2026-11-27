package hr.rkgarmory.app.feature.equipment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.ui.BoxIcon
import hr.rkgarmory.app.ui.EmptyState
import hr.rkgarmory.app.ui.FilterChips
import hr.rkgarmory.app.ui.ListActionBar
import hr.rkgarmory.app.ui.SearchField
import hr.rkgarmory.app.ui.StatusChip
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
    // Held here so the phone list keeps its place while a piece is open.
    val phoneListState = rememberLazyListState()
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
    // The phone list is lazy and scrolls whole; pages are for the tablet table.
    val pageAll = !phone && statusFilter == "Sve" && typeFilter == "Sve"
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

    LaunchedEffect(StaffSession.cachedInventory) {
        viewModel.open()
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
            history = StaffSession.cachedReservations?.let { pieceHistory(editing, it) },
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
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Traži šifru, vrstu ili ime",
                    modifier = Modifier.weight(1f),
                )
                if (!phone) AddButton(onClick = { creating = true })
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
                FilterChips(
                    options = (listOf("Sve") + inventoryStatuses).map { it to it },
                    selected = statusFilter,
                    onSelect = { statusFilter = it },
                )
                Spacer(Modifier.height(8.dp))
                FilterChips(
                    options = listOf("Sve" to "Sve") + inventoryTypes.map { (id, label) ->
                        id to typeTabLabel(label)
                    },
                    selected = typeFilter,
                    onSelect = { typeFilter = it },
                )
            }
        }
        HorizontalDivider()
        if (phone) {
            PhoneListBar(pieces = shown, onAdd = { creating = true })
        }
        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = { viewModel.reload() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (shown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        text = if (query.isBlank()) "Nema komada" else "Nema pronađenih",
                        icon = if (query.isBlank()) BoxIcon else Icons.Rounded.SearchOff,
                    )
                }
            } else if (phone) {
                PieceList(
                    pieces = shown,
                    state = phoneListState,
                    onOpen = { editingId = it },
                )
            } else {
                PieceTable(
                    pieces = shown,
                    onOpen = { editingId = it },
                    modifier = Modifier.padding(16.dp),
                )
            }
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

/**
 * One filter on the phone as a chip with a menu, like [FilterChips]: 48dp,
 * radius 12dp. While it shows everything (the first option, "Sve") it reads
 * [label] on `surface` with an outline; once it filters it reads the picked
 * value on `primaryContainer` with a check. The menu marks the picked one.
 */
@Composable
private fun FilterSelect(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val value = options.firstOrNull { it.first == selected }?.second.orEmpty()
    val active = selected != options.firstOrNull()?.first
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .semantics {
                    role = Role.DropdownList
                    contentDescription = "$label: $value"
                },
            shape = RoundedCornerShape(12.dp),
            color = if (active) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (active) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            border = if (active) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier
                    .padding(start = if (active) 12.dp else 16.dp, end = 8.dp)
                    .clearAndSetSemantics { },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (active) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = if (active) value else label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            options.forEach { (id, text) ->
                val picked = id == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            fontWeight = if (picked) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                    modifier = Modifier.semantics { this.selected = picked },
                    trailingIcon = if (picked) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text("Dodaj opremu", fontWeight = FontWeight.SemiBold)
    }
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
            LazyColumn {
                items(pieces, key = { it.id }) { piece ->
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
}

/**
 * Phone list: the pieces as rows of one rounded block. Lazy, so a few
 * hundred pieces scroll without pages.
 */
@Composable
private fun PieceList(
    pieces: List<Piece>,
    state: LazyListState,
    onOpen: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
    ) {
        itemsIndexed(pieces, key = { _, piece -> piece.id }) { index, piece ->
            val top = if (index == 0) 16.dp else 0.dp
            val bottom = if (index == pieces.lastIndex) 16.dp else 0.dp
            Column(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(top, top, bottom, bottom))
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PieceRow(piece, onOpen = { onOpen(piece.id) })
            }
        }
    }
}

/** Phone: how many pieces the filters show, and the add button. */
@Composable
private fun PhoneListBar(pieces: List<Piece>, onAdd: () -> Unit) {
    val issued = pieces.count { it.status == StatusIssuedLabel }
    ListActionBar(
        countText = "${pieces.size} ${pieceWord(pieces.size)} · $issued izdano",
        actionLabel = "Dodaj opremu",
        onAction = onAdd,
    )
}

private fun pieceWord(count: Int): String =
    if (count % 10 == 1 && count % 100 != 11) "komad" else "komada"

@Composable
private fun PieceRow(piece: Piece, onOpen: () -> Unit) {
    val size = piece.size.ifBlank { pieceSize(piece.type, piece.code) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TypeTile(piece.type)
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = piece.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(piece.type, size.takeIf { it.isNotBlank() }?.let { "vel. $it" })
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (piece.status == StatusIssuedLabel) {
                val holder = listOf(piece.holder.orEmpty(), piece.issuedOn.orEmpty())
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                if (holder.isNotEmpty()) {
                    Text(
                        text = holder,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (piece.note.isNotBlank()) {
                Text(
                    text = piece.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        StatusPill(piece.status)
    }
}

/** First two letters of the equipment type in a tinted square. */
@Composable
internal fun TypeTile(type: String, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = type.take(2).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
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
internal fun StatusPill(status: String) {
    val color = when (status) {
        StatusIssuedLabel -> StatusIssued
        "Neispravno" -> StatusBroken
        "Izgubljeno" -> StatusLost
        "Otpisano" -> StatusWrittenOff
        else -> StatusInStock
    }
    StatusChip(status, color)
}
