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
import com.example.cfvaname.data.NilaiCf
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.*
import com.example.cfvaname.viewmodel.NilaiCfViewModel

@Composable
fun NilaiCfScreen(
    padding: PaddingValues,
    viewModel: NilaiCfViewModel = viewModel()
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(VenamePrimaryDark, VenamePrimary)))
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stringResource(AppStrings.NilaiCf), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(stringResource(AppStrings.CertaintyFactor), color = MaterialTheme.colorScheme.onPrimary.copy(0.8f), fontSize = 13.sp)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.onPrimary.copy(0.2f)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.nilaiCfList.size}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text(stringResource(AppStrings.Level), color = MaterialTheme.colorScheme.onPrimary.copy(0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Info: distribusi otomatis
            Surface(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                color = VenamePrimary.copy(alpha = 0.12f)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, null, tint = VenamePrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(AppStrings.AutoDistributedValues),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Visual distribution bar
            if (uiState.nilaiCfList.isNotEmpty()) {
                NilaiDistributionBar(nilaiList = uiState.nilaiCfList)
                Spacer(Modifier.height(12.dp))
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VenamePrimary)
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(AppStrings.Loading), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
                uiState.nilaiCfList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.BarChart, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(AppStrings.NoCfValues), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
                else -> {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.nilaiCfList, key = { _, n -> n.id }) { index, nilaiCf ->
                            NilaiCfCard(
                                nilaiCf = nilaiCf,
                                index = index + 1,
                                total = uiState.nilaiCfList.size,
                                onEdit = { viewModel.showEditDialog(nilaiCf) },
                                onDelete = { viewModel.showDeleteDialog(nilaiCf) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { viewModel.showAddDialog() },
            containerColor = VenamePrimary, contentColor = Color.White, shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp)
        ) { Icon(Icons.Filled.Add, stringResource(AppStrings.AddCfValue)) }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    // Dialogs
    if (uiState.showAddDialog) {
        NilaiCfFormDialog(
            title = stringResource(AppStrings.AddCfValue),
            keterangan = uiState.formKeterangan,
            errorMessage = uiState.formError,
            isSaving = uiState.isSaving,
            previewValues = viewModel.previewDistribution(uiState.nilaiCfList.size),
            onKeteranganChange = { viewModel.onFormKeteranganChange(it) },
            onSave = { viewModel.saveNew() },
            onDismiss = { viewModel.hideAddDialog() }
        )
    }
    if (uiState.showEditDialog) {
        NilaiCfEditDialog(
            nilaiCf = uiState.selectedNilaiCf!!,
            keterangan = uiState.formKeterangan,
            errorMessage = uiState.formError,
            isSaving = uiState.isSaving,
            onKeteranganChange = { viewModel.onFormKeteranganChange(it) },
            onSave = { viewModel.saveEdit() },
            onDismiss = { viewModel.hideEditDialog() }
        )
    }
    if (uiState.showDeleteDialog && uiState.selectedNilaiCf != null) {
        NilaiCfDeleteDialog(
            nilaiCf = uiState.selectedNilaiCf!!,
            isSaving = uiState.isSaving,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }
}

// ===================================================
// VISUAL DISTRIBUTION BAR
// ===================================================
@Composable
fun NilaiDistributionBar(nilaiList: List<NilaiCf>) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(AppStrings.ValueDistribution), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))

            // Gradient bar
            Box(
                Modifier.fillMaxWidth().height(28.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(StatusError, StatusWarning, VenameAccent, StatusSuccess)))
            ) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    nilaiList.forEach { cf ->
                        val fraction = cf.nilai.toFloat().coerceIn(0f, 1f)
                        Box(contentAlignment = Alignment.Center) {
                            // Dot marker
                            Box(
                                Modifier.size(10.dp).clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Labels
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                nilaiList.forEach { cf ->
                    Text(
                        String.format("%.2f", cf.nilai),
                        fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ===================================================
// NILAI CF CARD
// ===================================================
@Composable
fun NilaiCfCard(nilaiCf: NilaiCf, index: Int, total: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    val currentLanguage = LocalLanguage.current
    
    // Color based on value
    val valueColor = when {
        nilaiCf.nilai <= 0.25 -> StatusError
        nilaiCf.nilai <= 0.50 -> StatusWarning
        nilaiCf.nilai <= 0.75 -> VenameAccent
        else -> StatusSuccess
    }

    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Nilai badge
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(valueColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    String.format("%.2f", nilaiCf.nilai),
                    color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(nilaiCf.keterangan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                // Progress bar
                LinearProgressIndicator(
                    progress = { nilaiCf.nilai.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = valueColor,
                    trackColor = valueColor.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    when (currentLanguage) {
                        "en" -> "Level $index of $total"
                        else -> "Level $index dari $total"
                    },
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Edit, stringResource(AppStrings.Edit), tint = VenamePrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, stringResource(AppStrings.Delete), tint = StatusError, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ===================================================
// ADD DIALOG (with preview distribution)
// ===================================================
@Composable
fun NilaiCfFormDialog(
    title: String, keterangan: String, errorMessage: String?, isSaving: Boolean,
    previewValues: List<Double>,
    onKeteranganChange: (String) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, stringResource(AppStrings.Close), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = keterangan, onValueChange = onKeteranganChange,
                    label = { Text(stringResource(AppStrings.Description)) },
                    placeholder = { Text(stringResource(AppStrings.DescriptionExample)) },
                    leadingIcon = { Icon(Icons.Filled.Label, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Preview distribusi setelah tambah
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = VenameAccent.copy(alpha = 0.12f)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoGraph, null, tint = VenameAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(AppStrings.PreviewAfterAdd), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            previewValues.forEach { v ->
                                Surface(shape = RoundedCornerShape(6.dp), color = VenamePrimary.copy(0.2f)) {
                                    Text(String.format("%.2f", v), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VenamePrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

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
                        Text(if (isSaving) stringResource(AppStrings.Loading) else stringResource(AppStrings.Save))
                    }
                }
            }
        }
    }
}

// ===================================================
// EDIT DIALOG (keterangan only)
// ===================================================
@Composable
fun NilaiCfEditDialog(
    nilaiCf: NilaiCf, keterangan: String, errorMessage: String?, isSaving: Boolean,
    onKeteranganChange: (String) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(AppStrings.EditCfValue), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, stringResource(AppStrings.Close), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(8.dp))

                // Current value (read-only)
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = VenamePrimary.copy(0.12f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, null, tint = VenamePrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(AppStrings.CfValue) + ": ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.2f", nilaiCf.nilai), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VenamePrimary)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(AppStrings.AutoCannotChangeManually), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = keterangan, onValueChange = onKeteranganChange,
                    label = { Text(stringResource(AppStrings.Description)) },
                    leadingIcon = { Icon(Icons.Filled.Label, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )

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
                        Text(if (isSaving) stringResource(AppStrings.Loading) else stringResource(AppStrings.Save))
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
fun NilaiCfDeleteDialog(nilaiCf: NilaiCf, isSaving: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(20.dp), containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(Modifier.size(56.dp).clip(CircleShape).background(StatusError.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DeleteForever, null, tint = StatusError, modifier = Modifier.size(30.dp))
            }
        },
        title = { Text(stringResource(AppStrings.DeleteCfValue), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(AppStrings.DeleteConfirmation), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = VenamePrimary.copy(alpha = 0.1f)) {
                            Text(String.format("%.2f", nilaiCf.nilai), color = VenamePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(nilaiCf.keterangan, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(stringResource(AppStrings.AllOtherCfValuesWillRecalculate), textAlign = TextAlign.Center, color = StatusWarning, fontSize = 12.sp)
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