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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(VenamePrimaryDark, VenamePrimary)))
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Nilai CF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Certainty Factor (0.00 - 1.00)", color = Color.White.copy(0.8f), fontSize = 13.sp)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.2f)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.nilaiCfList.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text("Level", color = Color.White.copy(0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Info: distribusi otomatis
            Surface(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                color = VenamePrimaryLight.copy(alpha = 0.3f)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, null, tint = VenamePrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Nilai terdistribusi otomatis secara merata dari 0,00 sampai 1,00 sesuai jumlah data.",
                        fontSize = 12.sp, color = VenamePrimaryDark, lineHeight = 16.sp
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
                            Text("Memuat data...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
                uiState.nilaiCfList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.BarChart, null, tint = TextSecondary.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Belum ada data nilai CF", color = TextSecondary, fontSize = 14.sp)
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
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 4.dp)
        ) { Icon(Icons.Filled.Add, "Tambah Nilai CF") }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    // Dialogs
    if (uiState.showAddDialog) {
        NilaiCfFormDialog(
            title = "Tambah Nilai CF Baru",
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
            Text("Distribusi Nilai", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                nilaiList.forEach { cf ->
                    Text(
                        String.format("%.2f", cf.nilai),
                        fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium
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
                Text(nilaiCf.keterangan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                // Progress bar
                LinearProgressIndicator(
                    progress = { nilaiCf.nilai.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = valueColor,
                    trackColor = valueColor.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(4.dp))
                Text("Level $index dari $total", fontSize = 11.sp, color = TextSecondary)
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
// ADD DIALOG (with preview distribution)
// ===================================================
@Composable
fun NilaiCfFormDialog(
    title: String, keterangan: String, errorMessage: String?, isSaving: Boolean,
    previewValues: List<Double>,
    onKeteranganChange: (String) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    IconButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, "Tutup", tint = TextSecondary) }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = keterangan, onValueChange = onKeteranganChange,
                    label = { Text("Keterangan") },
                    placeholder = { Text("Contoh: Cukup Yakin") },
                    leadingIcon = { Icon(Icons.Filled.Label, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Preview distribusi setelah tambah
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = VenameAccent.copy(alpha = 0.08f)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoGraph, null, tint = VenameAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Preview distribusi setelah ditambah:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VenameAccent)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            previewValues.forEach { v ->
                                Surface(shape = RoundedCornerShape(6.dp), color = VenamePrimary.copy(0.1f)) {
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
                    OutlinedButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Batal") }
                    Button(onClick = onSave, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary), enabled = !isSaving) {
                        if (isSaving) { CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                        Text(if (isSaving) "Menyimpan..." else "Simpan")
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
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Keterangan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    IconButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, "Tutup", tint = TextSecondary) }
                }
                Spacer(Modifier.height(8.dp))

                // Current value (read-only)
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = VenamePrimary.copy(0.08f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, null, tint = VenamePrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nilai: ", fontSize = 13.sp, color = TextSecondary)
                        Text(String.format("%.2f", nilaiCf.nilai), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VenamePrimary)
                        Text(" (otomatis, tidak bisa diubah manual)", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = keterangan, onValueChange = onKeteranganChange,
                    label = { Text("Keterangan") },
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
                    OutlinedButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Batal") }
                    Button(onClick = onSave, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary), enabled = !isSaving) {
                        if (isSaving) { CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                        Text(if (isSaving) "Menyimpan..." else "Simpan")
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
        title = { Text("Hapus Nilai CF?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Anda yakin ingin menghapus nilai CF ini?", textAlign = TextAlign.Center, color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = VenamePrimary.copy(alpha = 0.1f)) {
                            Text(String.format("%.2f", nilaiCf.nilai), color = VenamePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(nilaiCf.keterangan, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Semua nilai CF lainnya akan dihitung ulang otomatis.", textAlign = TextAlign.Center, color = StatusWarning, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = StatusError), shape = RoundedCornerShape(10.dp), enabled = !isSaving) {
                if (isSaving) { CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (isSaving) "Menghapus..." else "Hapus")
            }
        },
        dismissButton = { OutlinedButton(onClick = { if (!isSaving) onDismiss() }, shape = RoundedCornerShape(10.dp)) { Text("Batal") } }
    )
}