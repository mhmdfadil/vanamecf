package com.example.cfvaname.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.cfvaname.data.Gejala
import com.example.cfvaname.data.Hipotesis
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.viewmodel.GejalaViewModel

@Composable
fun GejalaScreen(
    padding: PaddingValues,
    gejalaViewModel: GejalaViewModel = viewModel()
) {
    val uiState by gejalaViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); gejalaViewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); gejalaViewModel.clearErrorMessage() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            GejalaHeader(totalData = uiState.gejalaList.size)
            Spacer(Modifier.height(12.dp))
            GejalaSearchBar(query = uiState.searchQuery, onQueryChange = { gejalaViewModel.onSearchQueryChange(it) })
            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VenamePrimary)
                            Spacer(Modifier.height(12.dp))
                            Text("Memuat data gejala...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
                uiState.gejalaList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.SearchOff, null, tint = TextSecondary.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (uiState.searchQuery.isNotBlank()) "Tidak ada gejala ditemukan untuk\n\"${uiState.searchQuery}\""
                                else "Belum ada data gejala",
                                color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.gejalaList, key = { _, g -> g.id }) { _, gejala ->
                            GejalaCard(
                                gejala = gejala,
                                hipotesisNama = gejalaViewModel.getHipotesisNama(gejala.hipotesisId),
                                hipotesisKode = gejalaViewModel.getHipotesisKode(gejala.hipotesisId),
                                onEdit = { gejalaViewModel.showEditDialog(gejala) },
                                onDelete = { gejalaViewModel.showDeleteDialog(gejala) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { gejalaViewModel.showAddDialog() },
            containerColor = VenamePrimary, contentColor = Color.White, shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 4.dp)
        ) { Icon(Icons.Filled.Add, "Tambah Gejala") }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (uiState.showAddDialog) {
        GejalaFormDialog(
            title = "Tambah Gejala Baru", kode = uiState.formKode, nama = uiState.formNama,
            selectedHipotesisId = uiState.formHipotesisId, hipotesisList = uiState.hipotesisList,
            errorMessage = uiState.formError, isSaving = uiState.isSaving,
            onKodeChange = { gejalaViewModel.onFormKodeChange(it) },
            onNamaChange = { gejalaViewModel.onFormNamaChange(it) },
            onHipotesisIdChange = { gejalaViewModel.onFormHipotesisIdChange(it) },
            onSave = { gejalaViewModel.saveNewGejala() },
            onDismiss = { gejalaViewModel.hideAddDialog() }
        )
    }
    if (uiState.showEditDialog) {
        GejalaFormDialog(
            title = "Edit Gejala", kode = uiState.formKode, nama = uiState.formNama,
            selectedHipotesisId = uiState.formHipotesisId, hipotesisList = uiState.hipotesisList,
            errorMessage = uiState.formError, isSaving = uiState.isSaving,
            onKodeChange = { gejalaViewModel.onFormKodeChange(it) },
            onNamaChange = { gejalaViewModel.onFormNamaChange(it) },
            onHipotesisIdChange = { gejalaViewModel.onFormHipotesisIdChange(it) },
            onSave = { gejalaViewModel.saveEditGejala() },
            onDismiss = { gejalaViewModel.hideEditDialog() }
        )
    }
    if (uiState.showDeleteDialog && uiState.selectedGejala != null) {
        GejalaDeleteDialog(
            gejala = uiState.selectedGejala!!, isSaving = uiState.isSaving,
            onConfirm = { gejalaViewModel.confirmDelete() },
            onDismiss = { gejalaViewModel.hideDeleteDialog() }
        )
    }
}

@Composable
fun GejalaHeader(totalData: Int) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(VenamePrimary, VenameAccent)))
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Data Gejala", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Kelola data gejala penyakit", color = Color.White.copy(0.8f), fontSize = 13.sp)
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.2f)) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalData", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Total", color = Color.White.copy(0.8f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun GejalaSearchBar(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query, onValueChange = onQueryChange,
        placeholder = { Text("Cari kode atau nama gejala...", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = VenamePrimary, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Filled.Clear, "Hapus", tint = TextSecondary, modifier = Modifier.size(20.dp)) }
            }
        },
        singleLine = true, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, unfocusedBorderColor = Color.LightGray, cursorColor = VenamePrimary, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    )
}

@Composable
fun GejalaCard(gejala: Gejala, hipotesisNama: String, hipotesisKode: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(VenamePrimary.copy(0.1f)), contentAlignment = Alignment.Center) {
                Text(gejala.kode, color = VenamePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(gejala.nama, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = VenameSecondary.copy(alpha = 0.1f)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Biotech, null, tint = VenameSecondary, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$hipotesisKode - $hipotesisNama", color = VenameSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Edit, "Edit", tint = VenamePrimary, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Delete, "Hapus", tint = StatusError, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GejalaFormDialog(
    title: String, kode: String, nama: String,
    selectedHipotesisId: Long, hipotesisList: List<Hipotesis>,
    errorMessage: String?, isSaving: Boolean,
    onKodeChange: (String) -> Unit, onNamaChange: (String) -> Unit,
    onHipotesisIdChange: (Long) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedHipotesis = hipotesisList.find { it.id == selectedHipotesisId }
    val displayText = if (selectedHipotesis != null) "${selectedHipotesis.kode} - ${selectedHipotesis.nama}" else ""

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    IconButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, "Tutup", tint = TextSecondary) }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = kode, onValueChange = onKodeChange,
                    label = { Text("Kode Gejala") }, placeholder = { Text("Contoh: G001") },
                    leadingIcon = { Icon(Icons.Filled.Tag, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nama, onValueChange = onNamaChange,
                    label = { Text("Nama Gejala") }, placeholder = { Text("Contoh: Demam tinggi") },
                    leadingIcon = { Icon(Icons.Filled.MedicalServices, null, tint = VenamePrimary) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Hipotesis Dropdown
                Text("Hipotesis", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Spacer(Modifier.height(4.dp))

                ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { if (!isSaving) dropdownExpanded = it }) {
                    OutlinedTextField(
                        value = displayText, onValueChange = {}, readOnly = true,
                        placeholder = { Text("Pilih hipotesis...") },
                        leadingIcon = { Icon(Icons.Filled.Biotech, null, tint = VenamePrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary),
                        enabled = !isSaving, modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                        if (hipotesisList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Tidak ada data hipotesis", color = TextSecondary, fontSize = 14.sp) },
                                onClick = { dropdownExpanded = false }
                            )
                        } else {
                            hipotesisList.forEach { hipotesis ->
                                val isSelected = hipotesis.id == selectedHipotesisId
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = if (isSelected) VenamePrimary.copy(0.15f) else VenameSecondary.copy(0.1f)) {
                                                Text(hipotesis.kode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) VenamePrimary else VenameSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Text(hipotesis.nama, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) VenamePrimary else TextPrimary)
                                        }
                                    },
                                    onClick = {
                                        onHipotesisIdChange(hipotesis.id)
                                        dropdownExpanded = false
                                    },
                                    trailingIcon = {
                                        if (isSelected) Icon(Icons.Filled.Check, null, tint = VenamePrimary, modifier = Modifier.size(18.dp))
                                    }
                                )
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

@Composable
fun GejalaDeleteDialog(gejala: Gejala, isSaving: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(20.dp), containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(Modifier.size(56.dp).clip(CircleShape).background(StatusError.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DeleteForever, null, tint = StatusError, modifier = Modifier.size(30.dp))
            }
        },
        title = { Text("Hapus Gejala?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Anda yakin ingin menghapus gejala ini?", textAlign = TextAlign.Center, color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = VenamePrimary.copy(alpha = 0.1f)) {
                            Text(gejala.kode, color = VenamePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(gejala.nama, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Data yang dihapus tidak dapat dikembalikan.", textAlign = TextAlign.Center, color = StatusError.copy(alpha = 0.7f), fontSize = 12.sp)
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