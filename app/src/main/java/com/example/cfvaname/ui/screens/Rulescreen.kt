package com.example.cfvaname.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cfvaname.data.Gejala
import com.example.cfvaname.data.NilaiCf
import com.example.cfvaname.data.Rule
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.*
import com.example.cfvaname.viewmodel.RuleViewModel

@Composable
fun RuleScreen(
    padding: PaddingValues,
    viewModel: RuleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearErrorMessage() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(VenamePrimaryDark, VenameAccent)))
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stringResource(AppStrings.Rules), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(stringResource(AppStrings.ConnectSymptomWithCfValue), color = Color.White.copy(0.8f), fontSize = 13.sp)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.2f)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.ruleList.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text(stringResource(AppStrings.Total), color = Color.White.copy(0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VenamePrimary)
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(AppStrings.LoadingRulesData), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
                uiState.ruleList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Rule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(AppStrings.NoRulesData), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
                else -> {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.ruleList, key = { _, r -> r.id }) { index, rule ->
                            val gejala = viewModel.getGejala(rule.gejalaId)
                            val nilaiCf = viewModel.getNilaiCf(rule.cfId)
                            RuleCard(
                                rule = rule, index = index + 1,
                                gejala = gejala, nilaiCf = nilaiCf,
                                onEdit = { viewModel.showEditDialog(rule) },
                                onDelete = { viewModel.showDeleteDialog(rule) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.showAddDialog() },
            containerColor = VenamePrimary, contentColor = Color.White, shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 4.dp)
        ) { Icon(Icons.Filled.Add, stringResource(AppStrings.AddRule)) }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    // Dialogs
    if (uiState.showAddDialog) {
        RuleFormDialog(
            title = stringResource(AppStrings.AddNewRule),
            gejalaList = viewModel.getAvailableGejala(),
            nilaiCfList = uiState.nilaiCfList,
            selectedGejalaId = uiState.formGejalaId,
            selectedCfId = uiState.formCfId,
            errorMessage = uiState.formError, isSaving = uiState.isSaving,
            onGejalaIdChange = { viewModel.onFormGejalaIdChange(it) },
            onCfIdChange = { viewModel.onFormCfIdChange(it) },
            onSave = { viewModel.saveNew() },
            onDismiss = { viewModel.hideAddDialog() }
        )
    }
    if (uiState.showEditDialog && uiState.selectedRule != null) {
        RuleFormDialog(
            title = stringResource(AppStrings.EditRule),
            gejalaList = viewModel.getAvailableGejala(excludeGejalaId = uiState.selectedRule!!.gejalaId),
            nilaiCfList = uiState.nilaiCfList,
            selectedGejalaId = uiState.formGejalaId,
            selectedCfId = uiState.formCfId,
            errorMessage = uiState.formError, isSaving = uiState.isSaving,
            onGejalaIdChange = { viewModel.onFormGejalaIdChange(it) },
            onCfIdChange = { viewModel.onFormCfIdChange(it) },
            onSave = { viewModel.saveEdit() },
            onDismiss = { viewModel.hideEditDialog() }
        )
    }
    if (uiState.showDeleteDialog && uiState.selectedRule != null) {
        val rule = uiState.selectedRule!!
        val gejala = viewModel.getGejala(rule.gejalaId)
        val nilaiCf = viewModel.getNilaiCf(rule.cfId)
        RuleDeleteDialog(
            gejalaText = if (gejala != null) "${gejala.kode} - ${gejala.nama}" else "ID: ${rule.gejalaId}",
            nilaiCfText = if (nilaiCf != null) "${nilaiCf.keterangan} (${String.format("%.2f", nilaiCf.nilai)})" else "ID: ${rule.cfId}",
            isSaving = uiState.isSaving,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }
}

// ===================================================
// RULE CARD
// ===================================================
@Composable
fun RuleCard(rule: Rule, index: Int, gejala: Gejala?, nilaiCf: NilaiCf?, onEdit: () -> Unit, onDelete: () -> Unit) {
    val currentLanguage = LocalLanguage.current
    
    val cfColor = when {
        nilaiCf == null -> MaterialTheme.colorScheme.onSurfaceVariant
        nilaiCf.nilai <= 0.25 -> StatusError
        nilaiCf.nilai <= 0.50 -> StatusWarning
        nilaiCf.nilai <= 0.75 -> VenameAccent
        else -> StatusSuccess
    }

    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Index badge
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(VenamePrimary.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("#$index", color = VenamePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text("${stringResource(AppStrings.Rule)} #$index", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) { Icon(Icons.Filled.Edit, stringResource(AppStrings.Edit), tint = VenamePrimary, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) { Icon(Icons.Filled.Delete, stringResource(AppStrings.Delete), tint = StatusError, modifier = Modifier.size(18.dp)) }
            }

            Spacer(Modifier.height(10.dp))

            // Gejala row
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = VenamePrimary.copy(0.06f)) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MedicalServices, null, tint = VenamePrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(stringResource(AppStrings.Symptom), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        if (gejala != null) {
                            Text("${gejala.kode} - ${gejala.nama}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        } else {
                            Text("ID: ${rule.gejalaId} (${stringResource(AppStrings.IdNotFound)})", fontSize = 14.sp, color = StatusError)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Arrow
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ArrowDownward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Nilai CF row
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = cfColor.copy(0.08f)) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BarChart, null, tint = cfColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(AppStrings.CfValue), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        if (nilaiCf != null) {
                            Text(nilaiCf.keterangan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        } else {
                            Text("ID: ${rule.cfId} (${stringResource(AppStrings.IdNotFound)})", fontSize = 14.sp, color = StatusError)
                        }
                    }
                    if (nilaiCf != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = cfColor.copy(0.15f)) {
                            Text(
                                String.format("%.2f", nilaiCf.nilai),
                                color = cfColor, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===================================================
// FORM DIALOG (Add / Edit) with 2 Dropdowns
// ===================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleFormDialog(
    title: String,
    gejalaList: List<Gejala>, nilaiCfList: List<NilaiCf>,
    selectedGejalaId: Long, selectedCfId: Long,
    errorMessage: String?, isSaving: Boolean,
    onGejalaIdChange: (Long) -> Unit, onCfIdChange: (Long) -> Unit,
    onSave: () -> Unit, onDismiss: () -> Unit
) {
    var gejalaExpanded by remember { mutableStateOf(false) }
    var cfExpanded by remember { mutableStateOf(false) }
    val selGejala = gejalaList.find { it.id == selectedGejalaId }
    val selCf = nilaiCfList.find { it.id == selectedCfId }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, stringResource(AppStrings.Close), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(16.dp))

                // === GEJALA DROPDOWN ===
                Text(stringResource(AppStrings.Symptom), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(expanded = gejalaExpanded, onExpandedChange = { if (!isSaving) gejalaExpanded = it }) {
                    OutlinedTextField(
                        value = if (selGejala != null) "${selGejala.kode} - ${selGejala.nama}" else "",
                        onValueChange = {}, readOnly = true,
                        placeholder = { Text(stringResource(AppStrings.SelectSymptomPlaceholder)) },
                        leadingIcon = { Icon(Icons.Filled.MedicalServices, null, tint = VenamePrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gejalaExpanded) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                        enabled = !isSaving, modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = gejalaExpanded, onDismissRequest = { gejalaExpanded = false }) {
                        if (gejalaList.isEmpty()) {
                            DropdownMenuItem(text = { Text(stringResource(AppStrings.NoSymptomAvailable), color = MaterialTheme.colorScheme.onSurfaceVariant) }, onClick = { gejalaExpanded = false })
                        } else {
                            gejalaList.forEach { g ->
                                val isSel = g.id == selectedGejalaId
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = if (isSel) VenamePrimary.copy(0.15f) else VenamePrimary.copy(0.08f)) {
                                                Text(g.kode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VenamePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(g.nama, fontSize = 14.sp, fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal, color = if (isSel) VenamePrimary else MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    },
                                    onClick = { onGejalaIdChange(g.id); gejalaExpanded = false },
                                    trailingIcon = { if (isSel) Icon(Icons.Filled.Check, null, tint = VenamePrimary, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // === NILAI CF DROPDOWN ===
                Text(stringResource(AppStrings.CfValue), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(expanded = cfExpanded, onExpandedChange = { if (!isSaving) cfExpanded = it }) {
                    OutlinedTextField(
                        value = if (selCf != null) "${selCf.keterangan} (${String.format("%.2f", selCf.nilai)})" else "",
                        onValueChange = {}, readOnly = true,
                        placeholder = { Text(stringResource(AppStrings.SelectCfValuePlaceholder)) },
                        leadingIcon = { Icon(Icons.Filled.BarChart, null, tint = VenamePrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cfExpanded) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                        enabled = !isSaving, modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = cfExpanded, onDismissRequest = { cfExpanded = false }) {
                        if (nilaiCfList.isEmpty()) {
                            DropdownMenuItem(text = { Text(stringResource(AppStrings.NoCfValueAvailable), color = MaterialTheme.colorScheme.onSurfaceVariant) }, onClick = { cfExpanded = false })
                        } else {
                            nilaiCfList.forEach { cf ->
                                val isSel = cf.id == selectedCfId
                                val cfColor = when {
                                    cf.nilai <= 0.25 -> StatusError
                                    cf.nilai <= 0.50 -> StatusWarning
                                    cf.nilai <= 0.75 -> VenameAccent
                                    else -> StatusSuccess
                                }
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = cfColor.copy(0.15f)) {
                                                Text(String.format("%.2f", cf.nilai), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cfColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(cf.keterangan, fontSize = 14.sp, fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal, color = if (isSel) VenamePrimary else MaterialTheme.colorScheme.onSurface)
                                        }
                                    },
                                    onClick = { onCfIdChange(cf.id); cfExpanded = false },
                                    trailingIcon = { if (isSel) Icon(Icons.Filled.Check, null, tint = VenamePrimary, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }
                }

                // Error
                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(10.dp), color = StatusError.copy(alpha = 0.1f)) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = StatusError, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage ?: "", color = StatusError, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Text(stringResource(AppStrings.Cancel)) }
                    Button(onClick = onSave, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary), enabled = !isSaving) {
                        if (isSaving) { CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                        Text(if (isSaving) stringResource(AppStrings.Saving) else stringResource(AppStrings.Save))
                    }
                }
            }
        }
    }
}

// ===================================================
// DELETE DIALOG
// ===================================================
@Composable
fun RuleDeleteDialog(gejalaText: String, nilaiCfText: String, isSaving: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(20.dp), containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(Modifier.size(56.dp).clip(CircleShape).background(StatusError.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DeleteForever, null, tint = StatusError, modifier = Modifier.size(30.dp))
            }
        },
        title = { Text(stringResource(AppStrings.DeleteRule), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(AppStrings.ConfirmDeleteRule), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MedicalServices, null, tint = VenamePrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(gejalaText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.BarChart, null, tint = VenameAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(nilaiCfText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = StatusError), shape = RoundedCornerShape(10.dp), enabled = !isSaving) {
                if (isSaving) { CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (isSaving) stringResource(AppStrings.Deleting) else stringResource(AppStrings.Delete))
            }
        },
        dismissButton = { OutlinedButton(onClick = { if (!isSaving) onDismiss() }, shape = RoundedCornerShape(10.dp)) { Text(stringResource(AppStrings.Cancel)) } }
    )
}