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
                            Text("Data Hipotesis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Kelola data hipotesis penyakit", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${uiState.hipotesisList.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Text("Total", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
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
                placeholder = { Text("Cari kode atau nama hipotesis...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = VenamePrimary, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { hipotesisViewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, "Hapus", tint = TextSecondary, modifier = Modifier.size(20.dp))
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
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VenamePrimary)
                            Spacer(Modifier.height(12.dp))
                            Text("Memuat data hipotesis...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
                uiState.hipotesisList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.SearchOff, null, tint = TextSecondary.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (uiState.searchQuery.isNotBlank()) "Tidak ditemukan untuk\n\"${uiState.searchQuery}\""
                                else "Belum ada data hipotesis",
                                color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center
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
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 4.dp)
        ) { Icon(Icons.Filled.Add, "Tambah Hipotesis") }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    // Dialogs
    if (uiState.showAddDialog) {
        HipotesisFormDialog(
            title = "Tambah Hipotesis Baru", uiState = uiState,
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
            title = "Edit Hipotesis", uiState = uiState,
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                Text(hipotesis.nama, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!hipotesis.deskripsi.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(hipotesis.deskripsi, fontSize = 12.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Edit, "Edit", tint = VenamePrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, "Hapus", tint = StatusError, modifier = Modifier.size(18.dp))
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
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Detail Hipotesis", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, "Tutup", tint = TextSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Kode badge
                Surface(shape = RoundedCornerShape(8.dp), color = VenameSecondary.copy(alpha = 0.1f)) {
                    Text(hipotesis.kode, color = VenameSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                Spacer(Modifier.height(12.dp))

                Text(hipotesis.nama, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(Modifier.height(16.dp))

                if (!hipotesis.deskripsi.isNullOrBlank()) {
                    Text("Deskripsi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = VenamePrimary)
                    Spacer(Modifier.height(4.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(hipotesis.deskripsi, fontSize = 14.sp, color = TextPrimary, lineHeight = 22.sp, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (!hipotesis.rekomendasi.isNullOrBlank()) {
                    Text("Rekomendasi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = StatusSuccess)
                    Spacer(Modifier.height(4.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = StatusSuccess.copy(alpha = 0.08f)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Lightbulb, null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(hipotesis.rekomendasi, fontSize = 14.sp, color = TextPrimary, lineHeight = 22.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary)) {
                    Text("Tutup")
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
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    IconButton(onClick = { if (!uiState.isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, "Tutup", tint = TextSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.formKode, onValueChange = onKodeChange,
                    label = { Text("Kode Hipotesis") }, placeholder = { Text("Contoh: H001") },
                    leadingIcon = { Icon(Icons.Filled.Tag, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.formNama, onValueChange = onNamaChange,
                    label = { Text("Nama Hipotesis") }, placeholder = { Text("Contoh: COVID-19") },
                    leadingIcon = { Icon(Icons.Filled.Biotech, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.formDeskripsi, onValueChange = onDeskripsiChange,
                    label = { Text("Deskripsi (opsional)") },
                    placeholder = { Text("Penjelasan tentang hipotesis...") },
                    shape = RoundedCornerShape(12.dp), minLines = 3, maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.formRekomendasi, onValueChange = onRekomendasiChange,
                    label = { Text("Rekomendasi (opsional)") },
                    placeholder = { Text("Tindakan yang disarankan...") },
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) { Text("Batal") }

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
                        Text(if (uiState.isSaving) "Menyimpan..." else "Simpan")
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
        title = { Text("Hapus Hipotesis?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Anda yakin ingin menghapus hipotesis ini?", textAlign = TextAlign.Center, color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = VenameSecondary.copy(alpha = 0.1f)) {
                            Text(hipotesis.kode, color = VenameSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(hipotesis.nama, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Gejala yang merujuk hipotesis ini akan terdampak.", textAlign = TextAlign.Center, color = StatusError.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = StatusError), shape = RoundedCornerShape(10.dp), enabled = !isSaving) {
                if (isSaving) { CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (isSaving) "Menghapus..." else "Hapus")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { if (!isSaving) onDismiss() }, shape = RoundedCornerShape(10.dp)) { Text("Batal") }
        }
    )
}