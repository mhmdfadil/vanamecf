package com.example.cfvaname.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cfvaname.data.*
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.viewmodel.KuesionerUserViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ======================================================================
// MAIN SCREEN
// ======================================================================
@Composable
fun KuesionerUserScreen(
    onBack: () -> Unit,
    onViewReports: () -> Unit,
    viewModel: KuesionerUserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    when {
        uiState.showHasil -> KuesionerUserHasilScreen(
            viewModel = viewModel,
            onBack = { viewModel.hideHasil() },
            onNewDiagnosis = {
                viewModel.hideHasil()
                viewModel.resetForm()
            },
            onViewReports = onViewReports
        )
        else -> KuesionerUserFormScreen(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            onBack = onBack
        )
    }
}

// ======================================================================
// FORM SCREEN - 30 Gejala Unik
// ======================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuesionerUserFormScreen(
    viewModel: KuesionerUserViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(VenamePrimary, VenameSecondary)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            "Kembali",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "Diagnosis Penyakit Udang",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text(
                        "Sistem Pakar Certainty Factor",
                        color = Color.White.copy(0.9f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info Petambak
            Surface(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Informasi Petambak",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VenamePrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.formNama,
                        onValueChange = { viewModel.onFormNamaChange(it) },
                        label = { Text("Nama Petambak") },
                        leadingIcon = { 
                            Icon(Icons.Filled.Person, null, tint = VenamePrimary) 
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VenamePrimary,
                            cursorColor = VenamePrimary
                        ),
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.formNoHp,
                        onValueChange = { viewModel.onFormNoHpChange(it) },
                        label = { Text("No. HP") },
                        leadingIcon = { 
                            Icon(Icons.Filled.Phone, null, tint = VenamePrimary) 
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VenamePrimary,
                            cursorColor = VenamePrimary
                        ),
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.formLokasi,
                        onValueChange = { viewModel.onFormLokasiChange(it) },
                        label = { Text("Lokasi Tambak") },
                        leadingIcon = { 
                            Icon(Icons.Filled.LocationOn, null, tint = VenamePrimary) 
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VenamePrimary,
                            cursorColor = VenamePrimary
                        ),
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.formUsiaUdang,
                        onValueChange = { viewModel.onFormUsiaUdangChange(it) },
                        label = { Text("Usia Udang (hari)") },
                        leadingIcon = { 
                            Icon(Icons.Filled.Pets, null, tint = VenamePrimary) 
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VenamePrimary,
                            cursorColor = VenamePrimary
                        ),
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Gejala Selection (30 gejala unik)
            Surface(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Checklist,
                            null,
                            tint = VenamePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pilih Gejala yang Dialami",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VenamePrimary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Pilih gejala dan tingkat keyakinan Anda",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    if (uiState.isLoading) {
                        Box(
                            Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VenamePrimary)
                        }
                    } else {
                        // Display 30 unique gejala
                        uiState.uniqueGejalaList.forEachIndexed { index, gejala ->
                            GejalaSelectItemUser(
                                gejala = gejala,
                                nilaiCfList = uiState.nilaiCfList,
                                selectedCfId = uiState.selectedGejalaCf[gejala.id],
                                enabled = !uiState.isSaving,
                                onSelect = { cfId -> 
                                    viewModel.toggleGejala(gejala.id, cfId) 
                                }
                            )
                            if (index < uiState.uniqueGejalaList.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                                )
                            }
                        }
                    }

                    if (uiState.selectedGejalaCf.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = StatusSuccess.copy(0.08f)
                        ) {
                            Row(
                                Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    null,
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${uiState.selectedGejalaCf.size} gejala dipilih",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusSuccess
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(visible = uiState.formError != null) {
                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = StatusError.copy(0.1f)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.ErrorOutline,
                            null,
                            tint = StatusError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            uiState.formError ?: "",
                            color = StatusError,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // Submit Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Button(
                onClick = { viewModel.submitKuesioner() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Filled.Send, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (uiState.isSaving) "Memproses..." else "Proses Diagnosis",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}

// ======================================================================
// GEJALA SELECT ITEM (SIMPLIFIED)
// ======================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GejalaSelectItemUser(
    gejala: Gejala,
    nilaiCfList: List<NilaiCf>,
    selectedCfId: Long?,
    enabled: Boolean,
    onSelect: (Long) -> Unit
) {
    val isSelected = selectedCfId != null && selectedCfId != 0L
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedCf = nilaiCfList.find { it.id == selectedCfId }

    val cfColor = when {
        selectedCf == null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
        selectedCf.nilai <= 0.25 -> StatusError
        selectedCf.nilai <= 0.50 -> StatusWarning
        selectedCf.nilai <= 0.75 -> VenameAccent
        else -> StatusSuccess
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = VenamePrimary.copy(0.12f)
            ) {
                Text(
                    gejala.kode,
                    color = VenamePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            gejala.nama,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it && enabled },
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedCf != null) cfColor.copy(0.12f) 
                       else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                onClick = { if (enabled) dropdownExpanded = true }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCf != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = cfColor.copy(0.2f)
                            ) {
                                Text(
                                    String.format("%.2f", selectedCf.nilai),
                                    color = cfColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 3.dp
                                    )
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                selectedCf.keterangan,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                "Belum Dipilih",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        tint = if (selectedCf != null) cfColor 
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                if (nilaiCfList.isEmpty()) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Tidak ada nilai CF",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            ) 
                        },
                        onClick = { dropdownExpanded = false }
                    )
                } else {
                    nilaiCfList.forEach { cf ->
                        val isSel = cf.id == selectedCfId
                        val itemCfColor = when {
                            cf.nilai <= 0.25 -> StatusError
                            cf.nilai <= 0.50 -> StatusWarning
                            cf.nilai <= 0.75 -> VenameAccent
                            else -> StatusSuccess
                        }
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = itemCfColor.copy(0.15f)
                                    ) {
                                        Text(
                                            String.format("%.2f", cf.nilai),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = itemCfColor,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 3.dp
                                            )
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        cf.keterangan,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.SemiBold 
                                                    else FontWeight.Normal,
                                        color = if (isSel) VenamePrimary 
                                               else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            onClick = { 
                                onSelect(cf.id)
                                dropdownExpanded = false 
                            },
                            trailingIcon = {
                                if (isSel) Icon(
                                    Icons.Filled.Check,
                                    null,
                                    tint = VenamePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
