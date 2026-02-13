package com.example.cfvaname.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.cfvaname.ui.localization.*
import com.example.cfvaname.viewmodel.KuesionerSummary
import com.example.cfvaname.viewmodel.KuesionerViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun KuesionerScreen(
    padding: PaddingValues,
    viewModel: KuesionerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearErrorMessage() }
    }

    when {
        uiState.showCreateForm -> KuesionerCreateForm(padding, viewModel)
        uiState.showHasil -> KuesionerHasilScreen(padding, viewModel)
        else -> KuesionerListScreen(padding, viewModel, snackbarHostState)
    }
}

// ======================================================================
// 1. LIST SCREEN (Ringkasan kuesioner)
// ======================================================================
@Composable
fun KuesionerListScreen(padding: PaddingValues, vm: KuesionerViewModel, snackbarHostState: SnackbarHostState) {
    val uiState by vm.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Brush.horizontalGradient(listOf(VenamePrimary, VenameSecondary))).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stringResource(AppStrings.Questionnaire_Title), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(stringResource(AppStrings.DiseaseQuestionnaireVename), color = MaterialTheme.colorScheme.onPrimary.copy(0.8f), fontSize = 13.sp)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.onPrimary.copy(0.2f)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.summaryList.size}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text(stringResource(AppStrings.Data), color = MaterialTheme.colorScheme.onPrimary.copy(0.8f), fontSize = 11.sp)
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
                            Text(stringResource(AppStrings.LoadingData), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
                uiState.summaryList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Assignment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(AppStrings.NoQuestionnaireData), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
                else -> {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.summaryList, key = { _, s -> s.kuesioner.id }) { index, summary ->
                            KuesionerSummaryCard(index + 1, summary,
                                onLihatHasil = { vm.showHasil(summary.kuesioner) },
                                onDelete = { vm.showDeleteDialog(summary.kuesioner) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        FloatingActionButton(onClick = { vm.showCreateForm() }, containerColor = VenamePrimary, contentColor = Color.White, shape = CircleShape, modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp)) {
            Icon(Icons.Filled.Add, stringResource(AppStrings.Questionnaire))
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    // Delete dialog
    if (uiState.showDeleteDialog && uiState.selectedKuesioner != null) {
        AlertDialog(
            onDismissRequest = { vm.hideDeleteDialog() }, shape = RoundedCornerShape(20.dp), containerColor = MaterialTheme.colorScheme.surface,
            icon = { Box(Modifier.size(56.dp).clip(CircleShape).background(StatusError.copy(0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.DeleteForever, null, tint = StatusError, modifier = Modifier.size(30.dp)) } },
            title = { Text(stringResource(AppStrings.DeleteQuestionnaire), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(String.format(stringResource(AppStrings.DeleteQuestionnaireConfirm), uiState.selectedKuesioner!!.namaPetambak), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { Button(onClick = { vm.confirmDelete() }, colors = ButtonDefaults.buttonColors(containerColor = StatusError), shape = RoundedCornerShape(10.dp), enabled = !uiState.isSaving) {
                if (uiState.isSaving) { CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (uiState.isSaving) stringResource(AppStrings.Deleting) else stringResource(AppStrings.Delete))
            } },
            dismissButton = { OutlinedButton(onClick = { vm.hideDeleteDialog() }, shape = RoundedCornerShape(10.dp)) { Text(stringResource(AppStrings.Cancel)) } }
        )
    }
}

@Composable
fun KuesionerSummaryCard(no: Int, summary: KuesionerSummary, onLihatHasil: () -> Unit, onDelete: () -> Unit) {
    val k = summary.kuesioner
    val dateStr = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).let { sdf -> val d = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(k.createdAt?.substringBefore("+") ?: ""); if (d != null) sdf.format(d) else k.createdAt?.take(10) ?: "-" } } catch (_: Exception) { k.createdAt?.take(10) ?: "-" }

    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(VenamePrimary.copy(0.1f)), contentAlignment = Alignment.Center) {
                    Text("$no", color = VenamePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(k.namaPetambak, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Pets, null, tint = VenameSecondary, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp))
                        Text("Usia: ${k.usiaUdang} hari", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = VenamePrimary, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp))
                        Text(k.lokasiTambak, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = VenameAccent.copy(0.1f)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Checklist, null, tint = VenameAccent, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp))
                        Text("${summary.gejalaCount} Gejala", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VenameAccent)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLihatHasil, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Icon(Icons.Filled.Analytics, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(AppStrings.ViewResults), fontSize = 13.sp)
                }
                OutlinedButton(onClick = onDelete, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(stringResource(AppStrings.Delete), fontSize = 13.sp)
                }
            }
        }
    }
}

// ======================================================================
// 2. CREATE FORM SCREEN - ✅ FIXED: Groups gejala by hipotesis, smart shared
// ======================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuesionerCreateForm(padding: PaddingValues, vm: KuesionerViewModel) {
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // ✅ PERBAIKAN: Group gejala_hipotesis by gejalaId untuk smart handling
    val gejalaMap = uiState.gejalaList.associateBy { it.id }
    val ghByGejalaId = uiState.gejalaHipotesisList.groupBy { it.gejalaId }
    val grouped = uiState.gejalaHipotesisList.groupBy { it.hipotesisId }

    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState)) {
        // Back + title
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.hideCreateForm() }) { Icon(Icons.Filled.ArrowBack, stringResource(AppStrings.Back)) }
            Text(stringResource(AppStrings.NewQuestionnaire), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(8.dp))

        // Info petambak
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(AppStrings.FarmerInformation), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VenamePrimary)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = uiState.formNama, onValueChange = { vm.onFormNamaChange(it) }, label = { Text(stringResource(AppStrings.FarmerName)) }, leadingIcon = { Icon(Icons.Filled.Person, null, tint = VenamePrimary) }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary), enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = uiState.formNoHp, onValueChange = { vm.onFormNoHpChange(it) }, label = { Text(stringResource(AppStrings.PhoneNumber_Header)) }, leadingIcon = { Icon(Icons.Filled.Phone, null, tint = VenamePrimary) }, singleLine = true, shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary), enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = uiState.formLokasi, onValueChange = { vm.onFormLokasiChange(it) }, label = { Text(stringResource(AppStrings.FarmLocation)) }, leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = VenamePrimary) }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary), enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = uiState.formUsiaUdang, onValueChange = { vm.onFormUsiaUdangChange(it) }, label = { Text(stringResource(AppStrings.ShrimpAge)) }, leadingIcon = { Icon(Icons.Filled.Pets, null, tint = VenamePrimary) }, singleLine = true, shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VenamePrimary, cursorColor = VenamePrimary), enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(16.dp))

        // ✅ Gejala grouped by hipotesis dengan smart shared handling
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Checklist, null, tint = VenamePrimary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                    Text(stringResource(AppStrings.SelectSymptomsAndConfidence), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VenamePrimary)
                }
                Spacer(Modifier.height(4.dp))
                Text(stringResource(AppStrings.SelectSymptomInstructions), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))

                val hipotesisKeys = grouped.keys.toList()
                hipotesisKeys.forEach { hipId ->
                    val ghItems = grouped[hipId] ?: emptyList()
                    val hip = uiState.hipotesisList.find { it.id == hipId }
                    if (hip != null) {
                        Text("${hip.kode} - ${hip.nama}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = VenamePrimaryDark, modifier = Modifier.padding(vertical = 12.dp))

                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ghItems.forEachIndexed { index, gh ->
                                val gejala = gejalaMap[gh.gejalaId]
                                if (gejala != null) {
                                    // ✅ PERBAIKAN: Cek apakah gejala ini sudah dipilih
                                    val isAlreadySelected = uiState.selectedGejalaCf.containsKey(gejala.id)
                                    
                                    GejalaSelectItem(
                                        gejala = gejala,
                                        gejalaId = gejala.id,
                                        nilaiCfList = uiState.nilaiCfList,
                                        selectedCfId = uiState.selectedGejalaCf[gejala.id],
                                        enabled = !uiState.isSaving,
                                        isAlreadySelectedInOtherHipotesis = isAlreadySelected && ghItems.indexOf(gh) > 0,
                                        onSelect = { cfId -> vm.toggleGejala(gejala.id, cfId) }
                                    )
                                    if (index < ghItems.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        if (hipId != hipotesisKeys.last()) {
                            HorizontalDivider(
                                Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                            )
                        }
                    }
                }

                if (uiState.selectedGejalaCf.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = StatusSuccess.copy(0.08f)) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = StatusSuccess, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp))
                            Text(String.format(stringResource(AppStrings.SymptomCountSelected), uiState.selectedGejalaCf.size), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StatusSuccess)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(visible = uiState.formError != null) {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = StatusError.copy(0.1f)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = StatusError, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    Text(uiState.formError ?: "", color = StatusError, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { vm.submitKuesioner() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary), enabled = !uiState.isSaving) {
            if (uiState.isSaving) { CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
            Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text(if (uiState.isSaving) stringResource(AppStrings.Saving) else stringResource(AppStrings.SaveAndViewResults), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/**
 * ✅ PERBAIKAN: Item pemilihan gejala dengan support shared gejala
 * - Accept gejalaId (bukan gejalaHipotesisId)
 * - Display "Shared" badge jika gejala sudah dipilih di hipotesis lain
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GejalaSelectItem(
    gejala: Gejala,
    gejalaId: Long,
    nilaiCfList: List<NilaiCf>,
    selectedCfId: Long?,
    enabled: Boolean,
    isAlreadySelectedInOtherHipotesis: Boolean = false,
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(6.dp), color = VenamePrimary.copy(0.12f)) {
                Text(gejala.kode, color = VenamePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
            // ✅ Badge untuk gejala yang sudah dipilih di hipotesis lain
            if (isAlreadySelectedInOtherHipotesis && isSelected) {
                Surface(shape = RoundedCornerShape(6.dp), color = StatusSuccess.copy(0.15f)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = StatusSuccess, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Shared", color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
            }
        }
        
        Text(gejala.nama, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it && enabled }, modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedCf != null) cfColor.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                onClick = { if (enabled) dropdownExpanded = true }
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        if (selectedCf != null) {
                            Surface(shape = RoundedCornerShape(4.dp), color = cfColor.copy(0.2f)) {
                                Text(String.format("%.2f", selectedCf.nilai), color = cfColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(selectedCf.keterangan, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        } else {
                            Text(stringResource(AppStrings.NotSet), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                    Icon(Icons.Filled.ArrowDropDown, null, tint = if (selectedCf != null) cfColor else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                if (nilaiCfList.isEmpty()) {
                    DropdownMenuItem(text = { Text(stringResource(AppStrings.NoCfValueAvailable), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }, onClick = { dropdownExpanded = false })
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
                                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = itemCfColor.copy(0.15f)) {
                                        Text(String.format("%.2f", cf.nilai), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = itemCfColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(cf.keterangan, fontSize = 13.sp, fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal, color = if (isSel) VenamePrimary else MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            },
                            onClick = { onSelect(cf.id); dropdownExpanded = false },
                            trailingIcon = { if (isSel) Icon(Icons.Filled.Check, null, tint = VenamePrimary, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        }
    }
}

// ======================================================================
// 3. HASIL SCREEN - Updated to use gejala_hipotesis mapping
// ======================================================================
@Composable
fun KuesionerHasilScreen(padding: PaddingValues, vm: KuesionerViewModel) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val k = uiState.hasilKuesioner ?: return

    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.hideHasil() }) { Icon(Icons.Filled.ArrowBack, stringResource(AppStrings.Back)) }
            Text(stringResource(AppStrings.DiagnosisResults), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            TextButton(onClick = { exportPdf(context, k, uiState.hasilResults, uiState.hasilKuesionerDataList, uiState.allGejalaMap, uiState.allNilaiCfMap, uiState.allGejalaHipotesisMap) }) {
                Icon(Icons.Filled.PictureAsPdf, null, tint = StatusError, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(stringResource(AppStrings.ExportPdf), color = StatusError, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = VenamePrimary) }
            return@Column
        }

        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = VenamePrimary.copy(0.06f)) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(AppStrings.FarmerInformation), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VenamePrimary)
                Spacer(Modifier.height(8.dp))
                InfoRow(stringResource(AppStrings.FarmerName), k.namaPetambak)
                InfoRow(stringResource(AppStrings.PhoneNumber_Header), k.noHp)
                InfoRow(stringResource(AppStrings.FarmLocation), k.lokasiTambak)
                InfoRow(stringResource(AppStrings.ShrimpAge), "${k.usiaUdang} hari")
            }
        }
        Spacer(Modifier.height(12.dp))

        // ✅ Gejala yang dipilih - resolve via gejala_hipotesis dan gejalaId
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(AppStrings.SelectedSymptoms), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VenamePrimary)
                Spacer(Modifier.height(8.dp))
                uiState.hasilKuesionerDataList.forEachIndexed { idx, data ->
                    val gh = uiState.allGejalaHipotesisMap[data.gejalaHipotesisId]
                    val gejala = if (gh != null) uiState.allGejalaMap[gh.gejalaId] else null
                    val nilaiCf = uiState.allNilaiCfMap[data.cfValue]
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = VenamePrimary.copy(0.1f)) { Text(gejala?.kode ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VenamePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
                        Spacer(Modifier.width(8.dp))
                        Text(gejala?.nama ?: "-", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${nilaiCf?.keterangan ?: "-"} (${String.format("%.2f", nilaiCf?.nilai ?: 0.0)})", fontSize = 11.sp, color = VenameSecondary)
                    }
                    if (idx < uiState.hasilKuesionerDataList.size - 1) HorizontalDivider(Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Text(stringResource(AppStrings.CfCalculationDetails), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))

        uiState.hasilResults.forEach { result ->
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${result.hipotesis.kode} - ${result.hipotesis.nama}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VenamePrimaryDark, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(8.dp), color = VenamePrimary.copy(0.1f)) { Text("${result.percentage}%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VenamePrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                        Column(Modifier.padding(8.dp)) {
                            Row(Modifier.fillMaxWidth()) {
                                Text(stringResource(AppStrings.SymptomsTable), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.4f))
                                Text(stringResource(AppStrings.CfExpertTableHeader), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                Text(stringResource(AppStrings.CfUserTableHeader), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                Text(stringResource(AppStrings.CfSymptomTableHeader), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                Text(stringResource(AppStrings.CfCombineTableHeader), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            result.steps.forEach { step ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text(step.gejalaKode, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1.4f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(String.format("%.2f", step.cfPakar), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                    Text(String.format("%.2f", step.cfUser), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                    Text(String.format("%.4f", step.cfGejala), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                    Text(String.format("%.4f", step.cfSesudah), fontSize = 10.sp, color = VenamePrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Row(Modifier.fillMaxWidth()) {
                                Text(stringResource(AppStrings.FinalCfCombine), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(3.5f), textAlign = TextAlign.End)
                                Text(String.format("%.4f", result.cfCombine) + " (${result.percentage}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VenamePrimary, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), color = VenamePrimaryLight.copy(0.3f)) {
                        Column(Modifier.padding(8.dp)) {
                            Text(stringResource(AppStrings.Formula), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VenamePrimaryDark)
                            Text(stringResource(AppStrings.CfFormulaSymptom), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(AppStrings.CfFormulaCombine), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (uiState.hasilResults.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(AppStrings.RankingResults), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))

            uiState.hasilResults.forEachIndexed { idx, res ->
                val color = when { res.percentage > 50 -> StatusSuccess; res.percentage > 30 -> StatusWarning; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = color.copy(0.06f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(0.15f)), contentAlignment = Alignment.Center) { Text("#${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${res.hipotesis.kode} - ${res.hipotesis.nama}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(progress = { (res.percentage / 100f).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = color, trackColor = color.copy(0.15f))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("${res.percentage}%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            val mainResult = uiState.hasilResults.first()
            Spacer(Modifier.height(12.dp))
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = StatusSuccess.copy(0.06f), shadowElevation = 2.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(AppStrings.DiagnosisSummary), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StatusSuccess)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(AppStrings.MainDiagnosis) + ": ${mainResult.hipotesis.nama} (${mainResult.percentage}%)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (!mainResult.hipotesis.deskripsi.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text("Deskripsi: ${mainResult.hipotesis.deskripsi}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp) }
                    if (!mainResult.hipotesis.rekomendasi.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = VenamePrimary.copy(0.06f)) { Row(Modifier.padding(10.dp)) { Icon(Icons.Filled.Lightbulb, null, tint = VenamePrimary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Rekomendasi: ${mainResult.hipotesis.rekomendasi}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp) } } }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ======================================================================
// 4. PDF EXPORT (Tanpa perubahan - sudah di file asli)
// ======================================================================

private class PdfBuilder(private val context: Context) {

    val doc = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val marginLeft = 45f
    val marginRight = 45f
    val marginTop = 50f
    val marginBottom = 60f
    val contentWidth = pageWidth - marginLeft - marginRight

    var pageNum = 1
    var y = marginTop

    private var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
    private var page = doc.startPage(pageInfo)
    var canvas = page.canvas
        private set

    object Colors {
        val primary = android.graphics.Color.parseColor("#1A73E8")
        val primaryDark = android.graphics.Color.parseColor("#0D47A1")
        val primaryLight = android.graphics.Color.parseColor("#E3F2FD")
        val accent = android.graphics.Color.parseColor("#00897B")
        val textDark = android.graphics.Color.parseColor("#212121")
        val textMedium = android.graphics.Color.parseColor("#424242")
        val textLight = android.graphics.Color.parseColor("#757575")
        val textMuted = android.graphics.Color.parseColor("#9E9E9E")
        val border = android.graphics.Color.parseColor("#E0E0E0")
        val bgLight = android.graphics.Color.parseColor("#F5F7FA")
        val bgCard = android.graphics.Color.parseColor("#FAFBFC")
        val white = android.graphics.Color.WHITE
        val success = android.graphics.Color.parseColor("#2E7D32")
        val successBg = android.graphics.Color.parseColor("#E8F5E9")
        val warning = android.graphics.Color.parseColor("#E65100")
        val warningBg = android.graphics.Color.parseColor("#FFF3E0")
        val headerBg = android.graphics.Color.parseColor("#1A73E8")
        val headerText = android.graphics.Color.WHITE
        val tableHeaderBg = android.graphics.Color.parseColor("#E8EAF6")
        val tableRowAlt = android.graphics.Color.parseColor("#F5F5F5")
    }

    private val paint = Paint().apply { isAntiAlias = true }
    private val fillPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val strokePaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 1f
    }

    fun wrapText(text: String, textSize: Float, maxWidth: Float, bold: Boolean = false): List<String> {
        paint.textSize = textSize
        paint.isFakeBoldText = bold
        val words = text.replace("\n", " ").split(" ").filter { it.isNotEmpty() }
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                if (paint.measureText(word) > maxWidth) {
                    var remaining = word
                    while (remaining.isNotEmpty()) {
                        var end = remaining.length
                        while (end > 1 && paint.measureText(remaining.substring(0, end)) > maxWidth) end--
                        lines.add(remaining.substring(0, end))
                        remaining = remaining.substring(end)
                    }
                    currentLine = ""
                } else {
                    currentLine = word
                }
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        if (lines.isEmpty()) lines.add("")
        return lines
    }

    fun drawWrappedText(
        text: String, x: Float, textSize: Float, maxWidth: Float,
        color: Int = Colors.textDark, bold: Boolean = false, lineSpacing: Float = 1.35f
    ): Float {
        val lines = wrapText(text, textSize, maxWidth, bold)
        val lineHeight = textSize * lineSpacing
        paint.textSize = textSize
        paint.isFakeBoldText = bold
        paint.color = color
        var totalHeight = 0f
        for (line in lines) {
            ensureSpace(lineHeight + 2f)
            canvas.drawText(line, x, y, paint)
            y += lineHeight
            totalHeight += lineHeight
        }
        return totalHeight
    }

    fun ensureSpace(needed: Float) {
        if (y + needed > pageHeight - marginBottom) {
            drawPageFooter()
            doc.finishPage(page)
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            page = doc.startPage(pageInfo)
            canvas = page.canvas
            y = marginTop
            drawPageHeaderBar()
        }
    }

    private fun drawPageHeaderBar() {
        fillPaint.color = Colors.primary
        canvas.drawRect(marginLeft, marginTop - 8f, pageWidth - marginRight, marginTop - 5f, fillPaint)
        y = marginTop + 8f
    }

    private fun drawPageFooter() {
        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        paint.color = Colors.textMuted
        val footerY = pageHeight - 25f
        canvas.drawText("Sistem Cerdas Vename", marginLeft, footerY, paint)
        val pageText = "Halaman $pageNum"
        val pageTextWidth = paint.measureText(pageText)
        canvas.drawText(pageText, pageWidth - marginRight - pageTextWidth, footerY, paint)
        strokePaint.color = Colors.border
        strokePaint.strokeWidth = 0.5f
        canvas.drawLine(marginLeft, footerY - 10f, pageWidth - marginRight, footerY - 10f, strokePaint)
    }

    fun drawRoundedRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float, color: Int) {
        fillPaint.color = color
        val rectF = android.graphics.RectF(left, top, right, bottom)
        canvas.drawRoundRect(rectF, radius, radius, fillPaint)
    }

    fun drawRoundedRectStroke(left: Float, top: Float, right: Float, bottom: Float, radius: Float, color: Int, width: Float = 1f) {
        strokePaint.color = color
        strokePaint.strokeWidth = width
        val rectF = android.graphics.RectF(left, top, right, bottom)
        canvas.drawRoundRect(rectF, radius, radius, strokePaint)
    }

    fun drawDivider(color: Int = Colors.border, thickness: Float = 0.75f) {
        strokePaint.color = color
        strokePaint.strokeWidth = thickness
        canvas.drawLine(marginLeft, y, pageWidth - marginRight, y, strokePaint)
        y += 6f
    }

    fun space(amount: Float) { y += amount }

    fun drawDocumentHeader(title: String, subtitle: String) {
        val bannerHeight = 70f
        drawRoundedRect(marginLeft, y, pageWidth - marginRight, y + bannerHeight, 8f, Colors.headerBg)
        paint.textSize = 18f; paint.isFakeBoldText = true; paint.color = Colors.headerText
        canvas.drawText(title, marginLeft + 18f, y + 28f, paint)
        paint.textSize = 10f; paint.isFakeBoldText = false; paint.color = android.graphics.Color.argb(200, 255, 255, 255)
        canvas.drawText(subtitle, marginLeft + 18f, y + 45f, paint)
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        paint.textSize = 9f
        val dateWidth = paint.measureText(dateStr)
        canvas.drawText(dateStr, pageWidth - marginRight - dateWidth - 18f, y + 45f, paint)
        y += bannerHeight + 16f
    }

    fun drawSectionHeader(title: String, iconChar: String? = null) {
        ensureSpace(30f)
        fillPaint.color = Colors.primary
        canvas.drawRect(marginLeft, y, marginLeft + 4f, y + 16f, fillPaint)
        paint.textSize = 12f; paint.isFakeBoldText = true; paint.color = Colors.primaryDark
        canvas.drawText(title, marginLeft + 12f, y + 12f, paint)
        y += 24f
    }

    fun drawInfoCard(data: List<Pair<String, String>>) {
        ensureSpace(20f + data.size * 20f)
        val cardTop = y - 4f
        val rowHeight = 22f
        val cardHeight = data.size * rowHeight + 16f
        val labelWidth = 130f
        drawRoundedRect(marginLeft, cardTop, pageWidth - marginRight, cardTop + cardHeight, 6f, Colors.bgCard)
        drawRoundedRectStroke(marginLeft, cardTop, pageWidth - marginRight, cardTop + cardHeight, 6f, Colors.border)
        y = cardTop + 12f
        data.forEachIndexed { index, (label, value) ->
            if (index % 2 == 0) {
                fillPaint.color = Colors.bgLight
                canvas.drawRect(marginLeft + 2f, y - 10f, pageWidth - marginRight - 2f, y + rowHeight - 10f, fillPaint)
            }
            paint.textSize = 9.5f; paint.isFakeBoldText = false; paint.color = Colors.textLight
            canvas.drawText(label, marginLeft + 14f, y, paint)
            val valueMaxWidth = contentWidth - labelWidth - 30f
            val lines = wrapText(value, 9.5f, valueMaxWidth, bold = true)
            paint.textSize = 9.5f; paint.isFakeBoldText = true; paint.color = Colors.textDark
            canvas.drawText(lines.firstOrNull() ?: "-", marginLeft + labelWidth + 14f, y, paint)
            y += rowHeight
        }
        y += 6f
    }

    enum class Align { LEFT, CENTER, RIGHT }

    fun drawTable(
        columns: List<Pair<String, Float>>, rows: List<List<String>>,
        headerBgColor: Int = Colors.tableHeaderBg, headerTextColor: Int = Colors.primaryDark,
        fontSize: Float = 8.5f, headerFontSize: Float = 8.5f,
        rowPaddingV: Float = 6f, cellPaddingH: Float = 6f,
        columnAligns: List<Align>? = null, lastColumnHighlight: Boolean = false
    ) {
        val totalWeight = columns.sumOf { it.second.toDouble() }.toFloat()
        val colWidths = columns.map { (it.second / totalWeight) * contentWidth }
        val colXs = mutableListOf<Float>()
        var cx = marginLeft
        colWidths.forEach { w -> colXs.add(cx); cx += w }
        val aligns = columnAligns ?: columns.mapIndexed { i, _ -> if (i == 0) Align.LEFT else Align.CENTER }
        val tableLeft = marginLeft
        val tableRight = pageWidth - marginRight

        drawRoundedRectStroke(tableLeft, y, tableRight, y + 1f, 4f, Colors.border, 0.5f)

        ensureSpace(28f)
        val headerTop = y
        val headerHeight = 22f
        drawRoundedRect(tableLeft, headerTop, tableRight, headerTop + headerHeight, 4f, headerBgColor)
        paint.textSize = headerFontSize; paint.isFakeBoldText = true; paint.color = headerTextColor

        columns.forEachIndexed { i, (header, _) ->
            val maxW = colWidths[i] - cellPaddingH * 2
            val truncated = truncateText(header, headerFontSize, maxW, bold = true)
            val textW = paint.measureText(truncated)
            val drawX = when (aligns[i]) {
                Align.LEFT -> colXs[i] + cellPaddingH
                Align.CENTER -> colXs[i] + (colWidths[i] - textW) / 2f
                Align.RIGHT -> colXs[i] + colWidths[i] - cellPaddingH - textW
            }
            canvas.drawText(truncated, drawX, headerTop + 14.5f, paint)
        }
        y = headerTop + headerHeight + 1f

        rows.forEachIndexed { rowIdx, row ->
            val cellLines = row.mapIndexed { colIdx, cellText ->
                val maxW = colWidths.getOrElse(colIdx) { colWidths.last() } - cellPaddingH * 2
                wrapText(cellText, fontSize, maxW)
            }
            val maxLines = cellLines.maxOf { it.size }
            val lineHeight = fontSize * 1.35f
            val rowHeight = maxLines * lineHeight + rowPaddingV * 2
            ensureSpace(rowHeight + 2f)

            if (rowIdx % 2 == 0) {
                fillPaint.color = Colors.tableRowAlt
                canvas.drawRect(tableLeft, y, tableRight, y + rowHeight, fillPaint)
            }

            cellLines.forEachIndexed { colIdx, lines ->
                val colAlign = aligns.getOrElse(colIdx) { Align.LEFT }
                val isLastCol = colIdx == columns.size - 1 && lastColumnHighlight
                paint.textSize = fontSize; paint.isFakeBoldText = isLastCol
                paint.color = if (isLastCol) Colors.primary else Colors.textDark
                var cellY = y + rowPaddingV + fontSize
                lines.forEach { line ->
                    val textW = paint.measureText(line)
                    val drawX = when (colAlign) {
                        Align.LEFT -> colXs[colIdx] + cellPaddingH
                        Align.CENTER -> colXs[colIdx] + (colWidths[colIdx] - textW) / 2f
                        Align.RIGHT -> colXs[colIdx] + colWidths[colIdx] - cellPaddingH - textW
                    }
                    canvas.drawText(line, drawX, cellY, paint)
                    cellY += lineHeight
                }
            }

            strokePaint.color = Colors.border; strokePaint.strokeWidth = 0.3f
            canvas.drawLine(tableLeft, y + rowHeight, tableRight, y + rowHeight, strokePaint)
            y += rowHeight
        }
        y += 4f
    }

    fun drawTableFooterRow(label: String, value: String) {
        ensureSpace(24f)
        val tableLeft = marginLeft; val tableRight = pageWidth - marginRight
        drawRoundedRect(tableLeft, y - 2f, tableRight, y + 18f, 4f, Colors.primaryLight)
        paint.textSize = 9.5f; paint.isFakeBoldText = true; paint.color = Colors.textDark
        val labelW = paint.measureText(label)
        val labelX = marginLeft + contentWidth * 0.55f - labelW
        canvas.drawText(label, labelX, y + 11f, paint)
        paint.color = Colors.primary; paint.isFakeBoldText = true
        canvas.drawText(value, marginLeft + contentWidth * 0.58f, y + 11f, paint)
        y += 24f
    }

    fun drawHighlightBox(
        title: String, content: String, bgColor: Int, borderColor: Int,
        titleColor: Int, contentColor: Int = Colors.textDark
    ) {
        val titleLines = wrapText(title, 11f, contentWidth - 30f, bold = true)
        val contentLines = wrapText(content, 9.5f, contentWidth - 30f)
        val totalHeight = titleLines.size * 15f + contentLines.size * 13f + 30f
        ensureSpace(totalHeight)
        val boxTop = y - 4f
        drawRoundedRect(marginLeft, boxTop, pageWidth - marginRight, boxTop + totalHeight, 6f, bgColor)
        drawRoundedRectStroke(marginLeft, boxTop, pageWidth - marginRight, boxTop + totalHeight, 6f, borderColor)
        fillPaint.color = borderColor
        canvas.drawRect(marginLeft, boxTop + 3f, marginLeft + 4f, boxTop + totalHeight - 3f, fillPaint)
        y = boxTop + 16f
        paint.textSize = 11f; paint.isFakeBoldText = true; paint.color = titleColor
        titleLines.forEach { line -> canvas.drawText(line, marginLeft + 16f, y, paint); y += 15f }
        y += 2f
        paint.textSize = 9.5f; paint.isFakeBoldText = false; paint.color = contentColor
        contentLines.forEach { line -> canvas.drawText(line, marginLeft + 16f, y, paint); y += 13f }
        y += 8f
    }

    fun drawRankingItem(rank: Int, label: String, percentage: Double) {
        ensureSpace(40f)
        val itemTop = y; val itemHeight = 36f; val pctInt = percentage.toInt()
        val rankColor = when { pctInt > 50 -> Colors.success; pctInt > 30 -> Colors.warning; else -> Colors.textLight }
        val rankBg = when { pctInt > 50 -> Colors.successBg; pctInt > 30 -> Colors.warningBg; else -> Colors.bgLight }
        drawRoundedRect(marginLeft, itemTop, pageWidth - marginRight, itemTop + itemHeight, 6f, rankBg)
        drawRoundedRectStroke(marginLeft, itemTop, pageWidth - marginRight, itemTop + itemHeight, 6f, Colors.border, 0.4f)

        val circleR = 11f; val circleX = marginLeft + 20f; val circleY = itemTop + itemHeight / 2f
        fillPaint.color = rankColor
        canvas.drawCircle(circleX, circleY, circleR, fillPaint)
        paint.textSize = 9f; paint.isFakeBoldText = true; paint.color = Colors.white
        val rankText = "#$rank"; val rankTextWidth = paint.measureText(rankText)
        canvas.drawText(rankText, circleX - rankTextWidth / 2f, circleY + 3.5f, paint)

        val labelStartX = marginLeft + 42f; val labelMaxW = contentWidth * 0.38f
        val labelLines = wrapText(label, 9.5f, labelMaxW, bold = false)
        paint.textSize = 9.5f; paint.isFakeBoldText = false; paint.color = Colors.textDark
        val labelBaseY = itemTop + if (labelLines.size == 1) (itemHeight / 2f + 3.5f) else (itemHeight / 2f - 3f)
        labelLines.take(2).forEachIndexed { i, line -> canvas.drawText(line, labelStartX, labelBaseY + i * 13f, paint) }

        val barStartX = marginLeft + contentWidth * 0.48f; val barMaxWidth = contentWidth * 0.34f
        val barHeight = 8f; val barY = itemTop + itemHeight / 2f - barHeight / 2f
        val barFillWidth = ((pctInt / 100f) * barMaxWidth).coerceAtLeast(0f)
        drawRoundedRect(barStartX, barY, barStartX + barMaxWidth, barY + barHeight, 4f, android.graphics.Color.argb(30, 0, 0, 0))
        if (barFillWidth > 2f) drawRoundedRect(barStartX, barY, barStartX + barFillWidth, barY + barHeight, 4f, rankColor)

        val pctText = "$pctInt%"
        paint.textSize = 12f; paint.isFakeBoldText = true; paint.color = rankColor
        val pctWidth = paint.measureText(pctText)
        canvas.drawText(pctText, pageWidth - marginRight - pctWidth - 12f, circleY + 4.5f, paint)
        y = itemTop + itemHeight + 6f
    }

    private fun truncateText(text: String, textSize: Float, maxWidth: Float, bold: Boolean = false): String {
        paint.textSize = textSize; paint.isFakeBoldText = bold
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + "…") > maxWidth) end--
        return if (end > 0) text.substring(0, end) + "…" else text.take(1)
    }

    fun finishDocument() {
        drawPageFooter()
        doc.finishPage(page)
    }
}

fun exportPdf(
    context: Context,
    kuesioner: Kuesioner,
    results: List<HipotesisResult>,
    dataList: List<KuesionerData>,
    gejalaMap: Map<Long, Gejala>,
    nilaiCfMap: Map<Long, NilaiCf>,
    gejalaHipotesisMap: Map<Long, GejalaHipotesis>
) {
    try {
        val builder = PdfBuilder(context)

        builder.drawDocumentHeader(
            "Laporan Hasil Diagnosa",
            "Sistem Cerdas Vename — Platform Monitoring Udang Vaname"
        )

        builder.drawSectionHeader("INFORMASI PETAMBAK")
        builder.drawInfoCard(
            listOf(
                "Nama Petambak" to kuesioner.namaPetambak,
                "No. HP" to kuesioner.noHp,
                "Lokasi Tambak" to kuesioner.lokasiTambak,
                "Usia Udang" to "${kuesioner.usiaUdang} hari"
            )
        )
        builder.space(8f)

        builder.drawSectionHeader("GEJALA YANG DIPILIH")

        val gejalaColumns = listOf(
            "No." to 0.5f, "Kode" to 0.8f, "Gejala" to 3.2f,
            "Tingkat Keyakinan" to 2.0f, "Nilai CF" to 0.8f
        )
        val gejalaAligns = listOf(
            PdfBuilder.Align.CENTER, PdfBuilder.Align.CENTER, PdfBuilder.Align.LEFT,
            PdfBuilder.Align.LEFT, PdfBuilder.Align.CENTER
        )
        val gejalaRows = dataList.mapIndexed { index, data ->
            val gh = gejalaHipotesisMap[data.gejalaHipotesisId]
            val g = if (gh != null) gejalaMap[gh.gejalaId] else null
            val cf = nilaiCfMap[data.cfValue]
            listOf(
                "${index + 1}",
                g?.kode ?: "-",
                g?.nama ?: "-",
                cf?.keterangan ?: "N/A",
                String.format("%.2f", cf?.nilai ?: 0.0)
            )
        }
        builder.drawTable(gejalaColumns, gejalaRows, columnAligns = gejalaAligns)
        builder.space(8f)

        if (results.isNotEmpty()) {
            builder.drawSectionHeader("HASIL DIAGNOSA")
            val mainResult = results.first()
            builder.drawHighlightBox(
                title = "Diagnosa Utama: ${mainResult.hipotesis.kode} - ${mainResult.hipotesis.nama}",
                content = "CF Combine: ${String.format("%.4f", mainResult.cfCombine)}  •  Persentase Keyakinan: ${mainResult.percentage}%",
                bgColor = PdfBuilder.Colors.primaryLight, borderColor = PdfBuilder.Colors.primary, titleColor = PdfBuilder.Colors.primaryDark
            )
        }

        builder.space(10f)
        builder.drawSectionHeader("DETAIL PERHITUNGAN CERTAINTY FACTOR")
        builder.drawHighlightBox(
            title = "Rumus Perhitungan",
            content = "CF Gejala = CF Pakar × CF User  •  CF Combine = CF_old + CF_new × (1 - CF_old)",
            bgColor = PdfBuilder.Colors.bgLight, borderColor = PdfBuilder.Colors.border,
            titleColor = PdfBuilder.Colors.textMedium, contentColor = PdfBuilder.Colors.textMedium
        )

        results.forEach { result ->
            builder.ensureSpace(50f)
            val hipHeaderText = "${result.hipotesis.kode} - ${result.hipotesis.nama}"
            val badgeText = "${result.percentage}%"
            val badgePaint = Paint().apply { isAntiAlias = true; textSize = 10f; isFakeBoldText = true }
            val badgeTextWidth = badgePaint.measureText(badgeText)
            val badgeTotalWidth = badgeTextWidth + 18f
            val maxHeaderWidth = builder.contentWidth - badgeTotalWidth - 16f
            val headerLines = builder.wrapText(hipHeaderText, 10.5f, maxHeaderWidth, bold = true)
            val headerBlockHeight = headerLines.size * 15f + 14f

            builder.drawRoundedRect(builder.marginLeft, builder.y - 4f, builder.pageWidth - builder.marginRight, builder.y + headerBlockHeight, 6f, PdfBuilder.Colors.bgLight)
            builder.drawRoundedRectStroke(builder.marginLeft, builder.y - 4f, builder.pageWidth - builder.marginRight, builder.y + headerBlockHeight, 6f, PdfBuilder.Colors.border, 0.5f)

            val accentPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL; color = PdfBuilder.Colors.primary }
            builder.canvas.drawRect(builder.marginLeft, builder.y - 1f, builder.marginLeft + 4f, builder.y + headerBlockHeight - 3f, accentPaint)

            val titleStartY = builder.y + 10f
            val titlePaint = Paint().apply { isAntiAlias = true; textSize = 10.5f; isFakeBoldText = true; color = PdfBuilder.Colors.primaryDark }
            headerLines.forEachIndexed { i, line -> builder.canvas.drawText(line, builder.marginLeft + 14f, titleStartY + i * 15f, titlePaint) }

            val badgeX = builder.pageWidth - builder.marginRight - badgeTotalWidth - 10f
            val badgeCenterY = builder.y + headerBlockHeight / 2f
            builder.drawRoundedRect(badgeX, badgeCenterY - 10f, badgeX + badgeTotalWidth, badgeCenterY + 10f, 5f, PdfBuilder.Colors.primary)
            badgePaint.color = PdfBuilder.Colors.white
            builder.canvas.drawText(badgeText, badgeX + (badgeTotalWidth - badgeTextWidth) / 2f, badgeCenterY + 4f, badgePaint)

            builder.y += headerBlockHeight + 8f

            val calcColumns = listOf("Gejala" to 2.4f, "CF Pakar" to 1.0f, "CF User" to 1.0f, "CF Gejala" to 1.1f, "CF Combine" to 1.1f)
            val calcAligns = listOf(PdfBuilder.Align.LEFT, PdfBuilder.Align.CENTER, PdfBuilder.Align.CENTER, PdfBuilder.Align.CENTER, PdfBuilder.Align.CENTER)
            val calcRows = result.steps.map { step ->
                listOf(step.gejalaKode, String.format("%.4f", step.cfPakar), String.format("%.4f", step.cfUser), String.format("%.4f", step.cfGejala), String.format("%.4f", step.cfSesudah))
            }
            builder.drawTable(calcColumns, calcRows, columnAligns = calcAligns, lastColumnHighlight = true)
            builder.drawTableFooterRow("CF Combine Akhir:", "${String.format("%.4f", result.cfCombine)} (${result.percentage}%)")
            builder.space(12f)
        }

        if (results.isNotEmpty()) {
            builder.drawSectionHeader("PERINGKAT HASIL DIAGNOSA")
            results.forEachIndexed { idx, res -> builder.drawRankingItem(rank = idx + 1, label = "${res.hipotesis.kode} - ${res.hipotesis.nama}", percentage = res.percentage) }
            builder.space(8f)

            builder.drawSectionHeader("KESIMPULAN")
            val main = results.first()
            builder.drawHighlightBox(title = "Diagnosa Utama: ${main.hipotesis.nama} (${main.percentage}%)", content = "Berdasarkan analisis Certainty Factor, penyakit yang paling mungkin menyerang udang adalah ${main.hipotesis.nama} dengan tingkat keyakinan ${main.percentage}%.", bgColor = PdfBuilder.Colors.successBg, borderColor = PdfBuilder.Colors.success, titleColor = PdfBuilder.Colors.success)
            builder.space(10f)
            if (!main.hipotesis.deskripsi.isNullOrBlank()) { builder.drawHighlightBox(title = "Deskripsi Penyakit", content = main.hipotesis.deskripsi, bgColor = PdfBuilder.Colors.bgCard, borderColor = PdfBuilder.Colors.border, titleColor = PdfBuilder.Colors.textMedium); builder.space(10f) }
            if (!main.hipotesis.rekomendasi.isNullOrBlank()) { builder.drawHighlightBox(title = "Rekomendasi Penanganan", content = main.hipotesis.rekomendasi, bgColor = PdfBuilder.Colors.primaryLight, borderColor = PdfBuilder.Colors.primary, titleColor = PdfBuilder.Colors.primaryDark) }
        }

        builder.space(16f)
        builder.ensureSpace(30f)
        builder.drawDivider()
        builder.space(4f)
        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 7.5f; color = PdfBuilder.Colors.textMuted }
        val timestamp = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(Date())
        builder.canvas.drawText("Dokumen ini digenerate secara otomatis oleh Sistem Cerdas Vename pada $timestamp.", builder.marginLeft, builder.y, footerPaint)
        builder.y += 11f
        builder.canvas.drawText("Hasil diagnosa bersifat informatif dan tidak menggantikan konsultasi dengan ahli perikanan.", builder.marginLeft, builder.y, footerPaint)

        builder.finishDocument()

        val documentsDir = File(context.getExternalFilesDir(null), "VENAME_Reports")
        documentsDir.mkdirs()
        val timestamp2 = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val fileName = "Diagnosa_${kuesioner.namaPetambak.replace(" ", "_")}_$timestamp2.pdf"
        val file = File(documentsDir, fileName)
        FileOutputStream(file).use { builder.doc.writeTo(it) }
        builder.doc.close()

        Toast.makeText(context, "PDF berhasil disimpan!", Toast.LENGTH_SHORT).show()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK }
        try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "PDF tersimpan di: ${file.absolutePath}", Toast.LENGTH_LONG).show() }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal export PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}