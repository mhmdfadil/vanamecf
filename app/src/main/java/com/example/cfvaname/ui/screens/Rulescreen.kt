package com.example.cfvaname.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cfvaname.data.NilaiCf
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.*
import com.example.cfvaname.viewmodel.*

@Composable
fun RuleScreen(
    padding: PaddingValues,
    viewModel: RuleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentLanguage = LocalLanguage.current

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
                        Text(stringResource(AppStrings.SelectCertaintyLevel), color = Color.White.copy(0.8f), fontSize = 13.sp)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.2f)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.hipotesisGroups.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
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
                uiState.hipotesisGroups.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Rule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(AppStrings.NoHypothesisWithSymptoms), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        Modifier.weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.hipotesisGroups, key = { it.hipotesis.id }) { group ->
                            HipotesisGroupCard(
                                group = group,
                                nilaiCfList = uiState.nilaiCfList,
                                currentLanguage = currentLanguage,
                                onCfSelected = { gejalaId, cfId ->
                                    viewModel.updateGejalaCfSelection(group.hipotesis.id, gejalaId, cfId)
                                },
                                getNilaiCf = { id -> viewModel.getNilaiCf(id) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // Save FAB
        if (uiState.hipotesisGroups.isNotEmpty()) {
            FloatingActionButton(
                onClick = { viewModel.saveAllRules() },
                containerColor = VenamePrimary, contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp)
            ) {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(AppStrings.Saving), fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(AppStrings.SaveAllRules), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}

// ===================================================
// HIPOTESIS GROUP CARD
// ===================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HipotesisGroupCard(
    group: HipotesisGroup,
    nilaiCfList: List<NilaiCf>,
    currentLanguage: String,
    onCfSelected: (Long, Long) -> Unit,
    getNilaiCf: (Long) -> NilaiCf?
) {
    var expanded by remember { mutableStateOf(true) }

    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Header - Hipotesis
            Surface(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                color = VenameSecondary.copy(0.1f)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hipotesis badge
                    Surface(shape = RoundedCornerShape(8.dp), color = VenameSecondary.copy(0.2f)) {
                        Text(
                            group.hipotesis.kode,
                            color = VenameSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            group.hipotesis.nama,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when (currentLanguage) {
                                "en" -> "${group.gejalaRules.size} Symptoms"
                                else -> "${group.gejalaRules.size} Gejala"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Gejala list
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    group.gejalaRules.forEachIndexed { index, gejalaRule ->
                        GejalaRuleItem(
                            gejalaRule = gejalaRule,
                            nilaiCfList = nilaiCfList,
                            onCfSelected = { cfId -> onCfSelected(gejalaRule.gejala.id, cfId) },
                            getNilaiCf = getNilaiCf
                        )
                        
                        // Divider between items (not after the last item)
                        if (index < group.gejalaRules.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===================================================
// GEJALA RULE ITEM - VERTICAL LAYOUT
// ===================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GejalaRuleItem(
    gejalaRule: GejalaRuleState,
    nilaiCfList: List<NilaiCf>,
    onCfSelected: (Long) -> Unit,
    getNilaiCf: (Long) -> NilaiCf?
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedCf = getNilaiCf(gejalaRule.selectedCfId)

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
        // Kode Gejala Badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = VenamePrimary.copy(0.12f)
        ) {
            Text(
                gejalaRule.gejala.kode,
                color = VenamePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Nama Gejala (Pertanyaan)
        Text(
            gejalaRule.gejala.nama,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )

        // CF Dropdown - Mengikuti lebar card
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedCf != null) cfColor.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                onClick = { dropdownExpanded = true }
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
                            // CF Value Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = cfColor.copy(0.2f)
                            ) {
                                Text(
                                    String.format("%.2f", selectedCf.nilai),
                                    color = cfColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            // CF Description
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
                                stringResource(AppStrings.NotSet),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                    
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        tint = if (selectedCf != null) cfColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                stringResource(AppStrings.NoCfValueAvailable), 
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            ) 
                        },
                        onClick = { dropdownExpanded = false }
                    )
                } else {
                    nilaiCfList.forEach { cf ->
                        val isSel = cf.id == gejalaRule.selectedCfId
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
                                    // CF Value Badge
                                    Surface(
                                        shape = RoundedCornerShape(4.dp), 
                                        color = itemCfColor.copy(0.15f)
                                    ) {
                                        Text(
                                            String.format("%.2f", cf.nilai),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = itemCfColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    // CF Description
                                    Text(
                                        cf.keterangan,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSel) VenamePrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            onClick = {
                                onCfSelected(cf.id)
                                dropdownExpanded = false
                            },
                            trailingIcon = {
                                if (isSel) {
                                    Icon(
                                        Icons.Filled.Check, 
                                        null, 
                                        tint = VenamePrimary, 
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}