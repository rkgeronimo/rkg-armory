package hr.gearmory.app.feature.issuance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.gearmory.app.remote.StaffUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IssuanceScreen(openId: Long? = null) {
    var pickedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var opened by rememberSaveable { mutableStateOf(false) }
    var walkIn by rememberSaveable { mutableStateOf(false) }
    var walkInUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    var appliedOpenId by rememberSaveable { mutableStateOf<Long?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedEquipment by rememberSaveable { mutableStateOf(loanEquipmentTypes.first()) }
    val issuanceViewModel: IssuanceViewModel = viewModel()
    val shown = issuanceViewModel.reservations
    val filtered = shown.filter {
        it.userName.contains(query, ignoreCase = true) ||
            it.excursion.orEmpty().contains(query, ignoreCase = true)
    }
    val selected = shown.firstOrNull { it.id == pickedId }
    val equipmentValues = issuanceViewModel.equipmentValues
    val equipmentSizes = issuanceViewModel.equipmentSizes
    val walkInUser = issuanceViewModel.users.firstOrNull { it.id == walkInUserId }
    val cancelIssuance = {
        issuanceViewModel.clearAll()
        pickedId = null
        walkIn = false
        walkInUserId = null
        opened = false
        selectedEquipment = loanEquipmentTypes.first()
    }

    LaunchedEffect(Unit) {
        issuanceViewModel.reload()
    }
    LaunchedEffect(walkIn) {
        if (walkIn) issuanceViewModel.loadUsers()
    }
    LaunchedEffect(openId) {
        if (openId != null && appliedOpenId != openId) {
            appliedOpenId = openId
            issuanceViewModel.clearAll()
            pickedId = openId
            walkIn = false
            walkInUserId = null
            opened = true
            selectedEquipment = loanEquipmentTypes.first()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (issuanceViewModel.loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }
        if (issuanceViewModel.notice != null) {
            Text(
                text = issuanceViewModel.notice.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        if (!opened || (selected == null && !walkIn)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        placeholder = { Text("Pretraži") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            pickedId = null
                            issuanceViewModel.reload()
                        },
                        enabled = !issuanceViewModel.loading,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Osvježi",
                        )
                    }
                }
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (query.isBlank()) "Nema zahtjeva" else "Nema pronađenih",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        filtered.forEach { reservation ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 96.dp)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reservation.userName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        val trip = reservation.excursion?.takeIf { it.isNotBlank() }
                                        if (trip != null) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = trip,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Button(
                                        onClick = {
                                            pickedId = reservation.id
                                            opened = true
                                        },
                                        modifier = Modifier.height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Text("Zaduživanje")
                                    }
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = {
                            issuanceViewModel.clearAll()
                            pickedId = null
                            walkIn = true
                            walkInUserId = null
                            opened = true
                            selectedEquipment = loanEquipmentTypes.first()
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "Novo izdavanje",
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (walkIn) {
                    UserPicker(
                        users = issuanceViewModel.users,
                        selected = walkInUser,
                        onSelect = { walkInUserId = it?.id },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                IssuanceEditor(
                    selectedEquipment = selectedEquipment,
                    onEquipmentSelected = { selectedEquipment = it },
                    equipmentValues = equipmentValues,
                    equipmentSizes = equipmentSizes,
                    requested = loanEquipmentTypes.associateWith { type ->
                        if (walkIn) {
                            "—"
                        } else {
                            requestedMark(loanApiKey[type]?.let { key -> selected?.equipment[key] })
                        }
                    },
                    onKey = { equipment, key ->
                        issuanceViewModel.pressKey(equipment, key)
                    },
                    onClearEquipment = issuanceViewModel::clearEquipment,
                    saving = issuanceViewModel.saving,
                    canIssue = !walkIn || walkInUser != null,
                    onCancel = cancelIssuance,
                    onIssue = {
                        if (walkIn) {
                            val userId = walkInUserId ?: return@IssuanceEditor
                            issuanceViewModel.createWalkIn(userId, cancelIssuance)
                        } else {
                            val id = selected?.id ?: return@IssuanceEditor
                            issuanceViewModel.issue(id, cancelIssuance)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPicker(
    users: List<StaffUser>,
    selected: StaffUser?,
    onSelect: (StaffUser?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected?.userName.orEmpty(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = { Text("Korisnik") },
            placeholder = { Text("Odaberi korisnika") },
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
            if (users.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Nema korisnika") },
                    onClick = { expanded = false },
                )
            } else {
                users.forEach { user ->
                    DropdownMenuItem(
                        text = { Text(user.userName) },
                        onClick = {
                            onSelect(user)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}
