package com.example.cfvaname.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cfvaname.data.Hipotesis
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.*
import com.example.cfvaname.viewmodel.HipotesisViewModel

@Composable
fun HipotesisScreen(
    padding: PaddingValues,
    hipotesisViewModel: HipotesisViewModel = viewModel()
) {
    val uiState by hipotesisViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); hipotesisViewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); hipotesisViewModel.clearErrorMessage() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(VenamePrimaryDark, VenameSecondary)))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(AppStrings.DataHipotesis), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(stringResource(AppStrings.ManageHypothesisData), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${uiState.hipotesisList.size}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Text(stringResource(AppStrings.TotalHypothesis), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { hipotesisViewModel.onSearchQueryChange(it) },
                placeholder = { Text(stringResource(AppStrings.SearchHypothesis), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = VenamePrimary, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { hipotesisViewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, stringResource(AppStrings.Delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VenamePrimary, unfocusedBorderColor = Color.LightGray,
                    cursorColor = VenamePrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                uiState.hipotesisList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.SearchOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (uiState.searchQuery.isNotBlank()) "${stringResource(AppStrings.NoHypothesisFoundFor)}\n\"${uiState.searchQuery}\""
                                else stringResource(AppStrings.NoHypothesis),
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.hipotesisList, key = { _, h -> h.id }) { _, hipotesis ->
                            HipotesisCard(
                                hipotesis = hipotesis,
                                onClick = { hipotesisViewModel.showDetailDialog(hipotesis) },
                                onEdit = { hipotesisViewModel.showEditDialog(hipotesis) },
                                onDelete = { hipotesisViewModel.showDeleteDialog(hipotesis) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { hipotesisViewModel.showAddDialog() },
            containerColor = VenamePrimary, contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp)
        ) { Icon(Icons.Filled.Add, stringResource(AppStrings.AddHypothesis)) }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    // Dialogs
    if (uiState.showAddDialog) {
        HipotesisFormDialog(
            title = stringResource(AppStrings.AddHypothesis), uiState = uiState,
            onKodeChange = { hipotesisViewModel.onFormKodeChange(it) },
            onNamaChange = { hipotesisViewModel.onFormNamaChange(it) },
            onDeskripsiChange = { hipotesisViewModel.onFormDeskripsiChange(it) },
            onRekomendasiChange = { hipotesisViewModel.onFormRekomendasiChange(it) },
            onSave = { hipotesisViewModel.saveNewHipotesis() },
            onDismiss = { hipotesisViewModel.hideAddDialog() }
        )
    }
    if (uiState.showEditDialog) {
        HipotesisFormDialog(
            title = stringResource(AppStrings.EditHypothesis), uiState = uiState,
            onKodeChange = { hipotesisViewModel.onFormKodeChange(it) },
            onNamaChange = { hipotesisViewModel.onFormNamaChange(it) },
            onDeskripsiChange = { hipotesisViewModel.onFormDeskripsiChange(it) },
            onRekomendasiChange = { hipotesisViewModel.onFormRekomendasiChange(it) },
            onSave = { hipotesisViewModel.saveEditHipotesis() },
            onDismiss = { hipotesisViewModel.hideEditDialog() }
        )
    }
    if (uiState.showDeleteDialog && uiState.selectedHipotesis != null) {
        HipotesisDeleteDialog(
            hipotesis = uiState.selectedHipotesis!!,
            isSaving = uiState.isSaving,
            onConfirm = { hipotesisViewModel.confirmDelete() },
            onDismiss = { hipotesisViewModel.hideDeleteDialog() }
        )
    }
    if (uiState.showDetailDialog && uiState.selectedHipotesis != null) {
        HipotesisDetailDialog(
            hipotesis = uiState.selectedHipotesis!!,
            onDismiss = { hipotesisViewModel.hideDetailDialog() }
        )
    }
}

// ===================================================
// HIPOTESIS CARD
// ===================================================
@Composable
fun HipotesisCard(
    hipotesis: Hipotesis,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(VenameSecondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(hipotesis.kode, color = VenameSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(hipotesis.nama, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!hipotesis.deskripsi.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(hipotesis.deskripsi, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
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
// DETAIL DIALOG
// ===================================================
@Composable
fun HipotesisDetailDialog(hipotesis: Hipotesis, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(AppStrings.DetailHypothesis), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, stringResource(AppStrings.Close), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Kode badge
                Surface(shape = RoundedCornerShape(8.dp), color = VenameSecondary.copy(alpha = 0.1f)) {
                    Text(hipotesis.kode, color = VenameSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                Spacer(Modifier.height(12.dp))

                Text(hipotesis.nama, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))

                if (!hipotesis.deskripsi.isNullOrBlank()) {
                    Text(stringResource(AppStrings.Description), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = VenamePrimary)
                    Spacer(Modifier.height(4.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(hipotesis.deskripsi, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (!hipotesis.rekomendasi.isNullOrBlank()) {
                    Text(stringResource(AppStrings.Recommendation), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = StatusSuccess)
                    Spacer(Modifier.height(4.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = StatusSuccess.copy(alpha = 0.08f)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Lightbulb, null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(hipotesis.rekomendasi, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary)) {
                    Text(stringResource(AppStrings.Close))
                }
            }
        }
    }
}

// ===================================================
// FORM DIALOG (Add / Edit)
// ===================================================
@Composable
fun HipotesisFormDialog(
    title: String,
    uiState: com.example.cfvaname.viewmodel.HipotesisUiState,
    onKodeChange: (String) -> Unit,
    onNamaChange: (String) -> Unit,
    onDeskripsiChange: (String) -> Unit,
    onRekomendasiChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!uiState.isSaving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { if (!uiState.isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, stringResource(AppStrings.Close), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.formKode, onValueChange = onKodeChange,
                    label = { Text(stringResource(AppStrings.HypothesisCode)) }, 
                    placeholder = { Text(stringResource(AppStrings.HypothesisCodeExample)) },
                    leadingIcon = { Icon(Icons.Filled.Tag, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.formNama, onValueChange = onNamaChange,
                    label = { Text(stringResource(AppStrings.HypothesisName)) }, 
                    placeholder = { Text(stringResource(AppStrings.HypothesisNameExample)) },
                    leadingIcon = { Icon(Icons.Filled.Biotech, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.formDeskripsi, onValueChange = onDeskripsiChange,
                    label = { Text(stringResource(AppStrings.HypothesisDescription) + " " + stringResource(AppStrings.Optional)) },
                    placeholder = { Text(stringResource(AppStrings.HypothesisDescriptionPlaceholder)) },
                    shape = RoundedCornerShape(12.dp), minLines = 3, maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.formRekomendasi, onValueChange = onRekomendasiChange,
                    label = { Text(stringResource(AppStrings.RecommendationOptional)) },
                    placeholder = { Text(stringResource(AppStrings.RecommendationPlaceholder)) },
                    shape = RoundedCornerShape(12.dp), minLines = 3, maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()
                )

                // Error
                AnimatedVisibility(visible = uiState.formError != null) {
                    Surface(Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(10.dp), color = StatusError.copy(alpha = 0.1f)) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = StatusError, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(uiState.formError ?: "", color = StatusError, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { if (!uiState.isSaving) onDismiss() },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) { Text(stringResource(AppStrings.Cancel)) }

                    Button(
                        onClick = onSave, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (uiState.isSaving) stringResource(AppStrings.Loading) else stringResource(AppStrings.Save))
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
fun HipotesisDeleteDialog(
    hipotesis: Hipotesis, isSaving: Boolean,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(Modifier.size(56.dp).clip(CircleShape).background(StatusError.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DeleteForever, null, tint = StatusError, modifier = Modifier.size(30.dp))
            }
        },
        title = { Text(stringResource(AppStrings.DeleteHypothesis), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(AppStrings.DeleteConfirmation), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = VenameSecondary.copy(alpha = 0.1f)) {
                            Text(hipotesis.kode, color = VenameSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(hipotesis.nama, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(stringResource(AppStrings.SymptomsReferencingWillBeAffected), textAlign = TextAlign.Center, color = StatusError.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = StatusError), shape = RoundedCornerShape(10.dp), enabled = !isSaving) {
                if (isSaving) { CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (isSaving) stringResource(AppStrings.Deleting) else stringResource(AppStrings.Delete))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { if (!isSaving) onDismiss() }, shape = RoundedCornerShape(10.dp)) { Text(stringResource(AppStrings.Cancel)) }
        }
    )
}