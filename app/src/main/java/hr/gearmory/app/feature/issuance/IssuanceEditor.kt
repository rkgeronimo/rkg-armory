package hr.gearmory.app.feature.issuance

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.gearmory.app.feature.equipment.typeTabLabel
import hr.gearmory.app.feature.inventory.inventorySizes
import hr.gearmory.app.feature.inventory.regulatorTypes

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
                        wanted = requested[equipment] != "—",
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
                        wanted = requested[equipment] != "—",
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
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            onKey = { key -> onKey(selectedEquipment, key) },
        )
        Spacer(Modifier.height(8.dp))
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
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun IssuanceActions(
    enabled: Boolean,
    saving: Boolean,
    onCancel: () -> Unit,
    onIssue: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCancel,
                enabled = !saving,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            ) {
                Text("Odustani")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { if (!saving) onIssue() },
                enabled = enabled || saving,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp,
                ),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
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
                    Text(
                        text = "Izdaj opremu",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
    }
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
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
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
private val WantedEquipment = Color(0xFFFFF3C4)

@Composable
private fun EquipmentChoice(
    equipment: String,
    selected: Boolean,
    value: String,
    size: String,
    onClick: () -> Unit,
    wanted: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val filled = loanReady(equipment, size, value)
    val container = when {
        filled -> FilledEquipment
        selected -> MaterialTheme.colorScheme.primary
        wanted -> WantedEquipment
        else -> MaterialTheme.colorScheme.surface
    }
    val content = when {
        filled -> Color.White
        selected -> MaterialTheme.colorScheme.onPrimary
        wanted -> MaterialTheme.colorScheme.onSurface
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = typeTabLabel(equipment),
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
                            Color.White.copy(alpha = 0.22f)
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
                        tint = if (filled || selected) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(if (compact) 14.dp else 18.dp),
                    )
                }
            }
            Spacer(Modifier.width(if (compact) 6.dp else 10.dp))
            Text(
                text = typeTabLabel(equipment),
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
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                shadowElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
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

@Composable
private fun NumericKeypad(
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
    onKey: (String) -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("X", "0", "⌫"),
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
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(keyHeight)
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
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                        ),
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
