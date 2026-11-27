package hr.rkgarmory.app.feature.issuance

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.rkgarmory.app.feature.equipment.typeTabLabel
import hr.rkgarmory.app.feature.inventory.inventorySizes
import hr.rkgarmory.app.feature.inventory.regulatorTypes
import hr.rkgarmory.app.ui.FormActions
import hr.rkgarmory.app.ui.ListIcon
import kotlinx.coroutines.launch

@Composable
internal fun IssuanceEditor(
    selectedEquipment: String,
    onEquipmentSelected: (String) -> Unit,
    equipmentValues: MutableMap<String, String>,
    equipmentSizes: MutableMap<String, String>,
    requested: Map<String, String>,
    onKey: (String, String) -> Unit,
    onClearEquipment: (String) -> Unit,
    saving: Boolean,
    canIssue: Boolean = true,
    onCancel: () -> Unit,
    onIssue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            CompactIssuanceEditor(
                selectedEquipment = selectedEquipment,
                onEquipmentSelected = onEquipmentSelected,
                equipmentValues = equipmentValues,
                equipmentSizes = equipmentSizes,
                requested = requested,
                onKey = onKey,
                onClearEquipment = onClearEquipment,
                saving = saving,
                canIssue = canIssue,
                onCancel = onCancel,
                onIssue = onIssue,
            )
        } else {
            WideIssuanceEditor(
                selectedEquipment = selectedEquipment,
                onEquipmentSelected = onEquipmentSelected,
                equipmentValues = equipmentValues,
                equipmentSizes = equipmentSizes,
                requested = requested,
                onKey = onKey,
                onClearEquipment = onClearEquipment,
                saving = saving,
                canIssue = canIssue,
                onCancel = onCancel,
                onIssue = onIssue,
            )
        }
    }
}

@Composable
private fun WideIssuanceEditor(
    selectedEquipment: String,
    onEquipmentSelected: (String) -> Unit,
    equipmentValues: MutableMap<String, String>,
    equipmentSizes: MutableMap<String, String>,
    requested: Map<String, String>,
    onKey: (String, String) -> Unit,
    onClearEquipment: (String) -> Unit,
    saving: Boolean,
    canIssue: Boolean,
    onCancel: () -> Unit,
    onIssue: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "OPREMA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp),
                )
                loanEquipmentTypes.forEach { equipment ->
                    EquipmentChoice(
                        equipment = equipment,
                        selected = equipment == selectedEquipment,
                        value = equipmentValues[equipment].orEmpty(),
                        size = equipmentSizes[equipment].orEmpty(),
                        requested = requested[equipment] ?: "—",
                        onClick = { onEquipmentSelected(equipment) },
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
        VerticalDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(22.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(modifier = Modifier.weight(0.7f)) {
                    val typed = equipmentValues[selectedEquipment].orEmpty()
                    EquipmentCodeInput(
                        value = loanField(
                            selectedEquipment,
                            equipmentSizes[selectedEquipment].orEmpty(),
                            typed,
                        ),
                        onClear = { onClearEquipment(selectedEquipment) },
                        clearEnabled = typed.isNotEmpty() ||
                            equipmentSizes[selectedEquipment].orEmpty().isNotEmpty(),
                        placeholder = if (selectedEquipment == LoanOlovo) "Količina" else "Oznaka opreme",
                    )
                }

                Column(
                    modifier = Modifier.weight(1.3f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EquipmentSizeSelector(
                        equipment = selectedEquipment,
                        selectedSize = equipmentSizes[selectedEquipment],
                        onSizeSelected = { equipmentSizes[selectedEquipment] = it },
                    )
                    NumericKeypad(
                        modifier = Modifier.fillMaxWidth(),
                        keyHeight = 56.dp,
                        onKey = { key -> onKey(selectedEquipment, key) },
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            IssuanceSummary(
                equipmentValues = equipmentValues,
                equipmentSizes = equipmentSizes,
                requested = requested,
            )
            }
            Spacer(Modifier.height(10.dp))
            IssuanceActions(
                enabled = canIssue && loanEquipmentTypes.any {
                    loanReady(it, equipmentSizes[it].orEmpty(), equipmentValues[it].orEmpty())
                },
                saving = saving,
                onCancel = onCancel,
                onIssue = onIssue,
            )
        }
    }
}

@Composable
private fun CompactIssuanceEditor(
    selectedEquipment: String,
    onEquipmentSelected: (String) -> Unit,
    equipmentValues: MutableMap<String, String>,
    equipmentSizes: MutableMap<String, String>,
    requested: Map<String, String>,
    onKey: (String, String) -> Unit,
    onClearEquipment: (String) -> Unit,
    saving: Boolean,
    canIssue: Boolean,
    onCancel: () -> Unit,
    onIssue: () -> Unit,
) {
    // Tall enough: equipment choice scrolls on top while the code field,
    // keypad and buttons stay fixed below, so no key ever sits under a
    // button. Too short (small phone, keyboard up for the member search):
    // the keypad scrolls with the rest and only the buttons stay. The
    // summary table opens in a bottom sheet from the key under 7.
    var showSummary by rememberSaveable { mutableStateOf(false) }
    if (showSummary) {
        SummarySheet(
            equipmentValues = equipmentValues,
            equipmentSizes = equipmentSizes,
            requested = requested,
            onClose = { showSummary = false },
        )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val keypadFixed = maxHeight >= CompactKeypadMinHeight
        val choices: @Composable () -> Unit = {
            Text(
                text = "OPREMA",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            loanEquipmentTypes.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { equipment ->
                        EquipmentChoice(
                            equipment = equipment,
                            selected = equipment == selectedEquipment,
                            value = equipmentValues[equipment].orEmpty(),
                            size = equipmentSizes[equipment].orEmpty(),
                            requested = requested[equipment] ?: "—",
                            compact = true,
                            onClick = { onEquipmentSelected(equipment) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        val entry: @Composable () -> Unit = {
            Spacer(Modifier.height(12.dp))
            val typed = equipmentValues[selectedEquipment].orEmpty()
            EquipmentCodeInput(
                value = loanField(
                    selectedEquipment,
                    equipmentSizes[selectedEquipment].orEmpty(),
                    typed,
                ),
                onClear = { onClearEquipment(selectedEquipment) },
                clearEnabled = typed.isNotEmpty() ||
                    equipmentSizes[selectedEquipment].orEmpty().isNotEmpty(),
                placeholder = if (selectedEquipment == LoanOlovo) "Količina" else "Oznaka opreme",
            )
            Spacer(Modifier.height(8.dp))
            EquipmentSizeSelector(
                equipment = selectedEquipment,
                selectedSize = equipmentSizes[selectedEquipment],
                onSizeSelected = { equipmentSizes[selectedEquipment] = it },
            )
            NumericKeypad(
                modifier = Modifier.fillMaxWidth(),
                keyHeight = 48.dp,
                onSummary = { showSummary = true },
                onKey = { key -> onKey(selectedEquipment, key) },
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                choices()
                if (!keypadFixed) entry()
            }
            if (keypadFixed) entry()
            Spacer(Modifier.height(12.dp))
            IssuanceActions(
                enabled = canIssue && loanEquipmentTypes.any {
                    loanReady(it, equipmentSizes[it].orEmpty(), equipmentValues[it].orEmpty())
                },
                saving = saving,
                onCancel = onCancel,
                onIssue = onIssue,
            )
        }
    }
}

/**
 * Below this editor height the fixed code field, size row, keypad and
 * buttons (about 470dp) would squeeze the three rows of equipment away.
 */
private val CompactKeypadMinHeight = 600.dp

/**
 * The issuance summary (requested and entered per equipment) in a bottom
 * sheet. Closes by dragging down, tapping outside or "Zatvori".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SummarySheet(
    equipmentValues: Map<String, String>,
    equipmentSizes: Map<String, String>,
    requested: Map<String, String>,
    onClose: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pregled opreme",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            // A drag can cancel the hide; then the sheet stays.
                            if (!sheetState.isVisible) onClose()
                        }
                    },
                ) {
                    Text("Zatvori", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            IssuanceSummary(
                equipmentValues = equipmentValues,
                equipmentSizes = equipmentSizes,
                requested = requested,
            )
        }
    }
}

@Composable
private fun IssuanceActions(
    enabled: Boolean,
    saving: Boolean,
    onCancel: () -> Unit,
    onIssue: () -> Unit,
) {
    FormActions(
        saving = saving,
        canSave = enabled,
        onCancel = onCancel,
        onSave = onIssue,
        saveLabel = "Izdaj opremu",
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentCodeInput(
    value: String,
    onClear: () -> Unit,
    clearEnabled: Boolean,
    placeholder: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            readOnly = true,
            placeholder = { Text(placeholder) },
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp,
            ),
            trailingIcon = if (clearEnabled) {
                {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Obriši oznaku",
                        )
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}

private val FilledEquipment = Color(0xFF1B7F4E)
/** Requested but not entered yet; a tint so its text reads in both themes. */
private val WantedEquipment = Color(0xFFE0A800)

/** "Odijelo (5)" when a size was requested, see [requestedMark]. */
private fun choiceLabel(equipment: String, requested: String): String {
    val name = typeTabLabel(equipment)
    // Regulators and masks come in one size.
    val sized = equipment != "Regulator" && equipment != LoanMaskaDisalica
    return if (!sized || requested == "—" || requested == "Da") name else "$name ($requested)"
}

@Composable
private fun EquipmentChoice(
    equipment: String,
    selected: Boolean,
    value: String,
    size: String,
    onClick: () -> Unit,
    requested: String = "—",
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val filled = loanReady(equipment, size, value)
    val wanted = requested != "—"
    val label = choiceLabel(equipment, requested)
    val container = when {
        filled -> FilledEquipment.copy(alpha = 0.16f)
        selected -> MaterialTheme.colorScheme.primary
        wanted -> WantedEquipment.copy(alpha = 0.22f)
        else -> MaterialTheme.colorScheme.surface
    }
    val content = when {
        filled -> lerp(FilledEquipment, MaterialTheme.colorScheme.onSurface, 0.4f)
        selected -> MaterialTheme.colorScheme.onPrimary
        wanted -> lerp(WantedEquipment, MaterialTheme.colorScheme.onSurface, 0.55f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .height(if (compact) 48.dp else 56.dp)
            .clickable(onClick = onClick),
        color = container,
        contentColor = content,
        border = if (!filled && !selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else {
            null
        },
        shape = RoundedCornerShape(12.dp),
    ) {
        if (compact) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // The tint alone is soft; the check says "done" at a glance.
                if (filled) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Popunjeno",
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            return@Surface
        }
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 20.dp else 28.dp)
                    .background(
                        color = if (filled) {
                            FilledEquipment.copy(alpha = 0.24f)
                        } else {
                            Color.Transparent
                        },
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                    .border(
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (!filled) {
                                if (selected) {
                                    Color.White.copy(alpha = 0.75f)
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            } else {
                                Color.Transparent
                            },
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (filled) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Popunjeno",
                        tint = content,
                        modifier = Modifier.size(if (compact) 14.dp else 18.dp),
                    )
                }
            }
            Spacer(Modifier.width(if (compact) 6.dp else 10.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = if (compact) {
                    MaterialTheme.typography.labelLarge
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = loanShown(equipment, size, value),
                style = if (compact) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun EquipmentSizeSelector(
    equipment: String,
    selectedSize: String?,
    onSizeSelected: (String) -> Unit,
) {
    if (equipment == "Regulator") {
        ChoiceRow(
            options = regulatorTypes.map { it.first to it.second },
            selected = selectedSize,
            onSelected = onSizeSelected,
        )
        return
    }
    val sizes = inventorySizes(equipment).map { it to it }

    ChoiceRow(options = sizes, selected = selectedSize, onSelected = onSizeSelected)
}

@Composable
private fun ChoiceRow(
    options: List<Pair<String, String>>,
    selected: String?,
    onSelected: (String) -> Unit,
) {
    if (options.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (label, value) ->
            val isSelected = value == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clickable { onSelected(value) },
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

/**
 * Digits and backspace. [onSummary], when given, fills the free key under
 * 7 with a button that opens the issuance summary.
 */
@Composable
private fun NumericKeypad(
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
    onSummary: (() -> Unit)? = null,
    onKey: (String) -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫"),
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        if (onSummary == null) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            Surface(
                                onClick = onSummary,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(keyHeight),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        ListIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Pregled",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                        return@forEach
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(keyHeight)
                            .clickable { onKey(key) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                        ),
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

@Composable
private fun IssuanceSummary(
    equipmentValues: Map<String, String>,
    equipmentSizes: Map<String, String>,
    requested: Map<String, String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                SummaryRow(name = "Oprema", requested = "Traženo", written = "Upisano", header = true)
            }
            loanEquipmentTypes.forEach { equipment ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SummaryRow(
                    name = equipment,
                    requested = requested[equipment] ?: "—",
                    written = loanShown(
                        equipment,
                        equipmentSizes[equipment].orEmpty(),
                        equipmentValues[equipment].orEmpty(),
                    ).ifBlank { "—" },
                    header = false,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    name: String,
    requested: String,
    written: String,
    header: Boolean,
) {
    val style = MaterialTheme.typography.bodySmall
    val color = if (header) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1.2f),
            style = style,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = requested,
            modifier = Modifier.weight(1f),
            style = style,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = written,
            modifier = Modifier.weight(1f),
            style = style,
            fontWeight = if (header || written == "—") FontWeight.Medium else FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
