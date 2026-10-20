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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

private val codePattern = Regex("^[A-Za-z0-9]{1,60}$")

/** Sizes per API type. A type missing here has no size. */
internal val typeSizes = mapOf(
    "suit" to (1..9).map { it.toString() },
    "boots" to (3..12).map { it.toString() },
    "fins" to listOf("S", "R", "XL"),
    "bcd" to listOf("XS", "S", "M", "L", "XL", "XXL"),
    "gloves" to listOf("S", "M", "L", "XL"),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun EquipmentCreateForm(
    saving: Boolean,
    notice: String?,
    onSave: (code: String, type: String, size: String, note: String) -> Unit,
    onCancel: () -> Unit,
) {
    var type by rememberSaveable { mutableStateOf("") }
    var size by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    val sizes = typeSizes[type].orEmpty()
    val codeText = code.trim()
    val sizeText = size.trim()
    val valid = type.isNotEmpty() &&
        codePattern.matches(codeText) &&
        sizeText.length <= 60
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .widthIn(max = 560.dp),
    ) {
        SectionLabel("Vrsta")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            inventoryTypes.forEach { (key, label) ->
                ChoiceChip(
                    label = label,
                    selected = key == type,
                    onClick = {
                        if (!saving && key != type) {
                            type = key
                            size = ""
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        SectionLabel("Veličina")
        if (sizes.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sizes.forEach { value ->
                    ChoiceChip(
                        label = value,
                        selected = value == sizeText,
                        onClick = { if (!saving) size = value },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = size,
            onValueChange = { size = it },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
        )
        Spacer(Modifier.height(20.dp))
        SectionLabel("Šifra")
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
        )
        Spacer(Modifier.height(20.dp))
        SectionLabel("Napomena")
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
        )
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
                onClick = { if (!saving && valid) onSave(codeText, type, sizeText, note.trim()) },
                enabled = valid || saving,
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
