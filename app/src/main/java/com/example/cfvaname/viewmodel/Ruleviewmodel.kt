package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data untuk menyimpan state rule per gejala
data class GejalaRuleState(
    val gejala: Gejala,
    val currentRule: Rule?,
    val selectedCfId: Long // CF yang dipilih user
)

// Group hipotesis dengan gejala-gejalanya
data class HipotesisGroup(
    val hipotesis: Hipotesis,
    val gejalaRules: List<GejalaRuleState>
)

data class RuleUiState(
    val hipotesisGroups: List<HipotesisGroup> = emptyList(),
    val nilaiCfList: List<NilaiCf> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RuleViewModel : ViewModel() {

    private val ruleRepo = RuleRepository()
    private val gejalaRepo = GejalaRepository()
    private val hipotesisRepo = HipotesisRepository()
    private val nilaiCfRepo = NilaiCfRepository()

    private val _uiState = MutableStateFlow(RuleUiState())
    val uiState: StateFlow<RuleUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val hipotesisResult = hipotesisRepo.getAll()
            val gejalaResult = gejalaRepo.getAll()
            val nilaiCfResult = nilaiCfRepo.getAll()
            val ruleResult = ruleRepo.getAll()

            if (hipotesisResult.isSuccess && gejalaResult.isSuccess &&
                nilaiCfResult.isSuccess && ruleResult.isSuccess) {

                val hipotesisList = hipotesisResult.getOrNull() ?: emptyList()
                val gejalaList = gejalaResult.getOrNull() ?: emptyList()
                val nilaiCfList = nilaiCfResult.getOrNull() ?: emptyList()
                val ruleList = ruleResult.getOrNull() ?: emptyList()

                val groups = hipotesisList.mapNotNull { hipotesis ->
                    val gejalasForThisHipotesis = gejalaList.filter { it.hipotesisId == hipotesis.id }
                    if (gejalasForThisHipotesis.isEmpty()) return@mapNotNull null

                    val gejalaRules = gejalasForThisHipotesis.map { gejala ->
                        val existingRule = ruleList.find { it.gejalaId == gejala.id }
                        GejalaRuleState(
                            gejala = gejala,
                            currentRule = existingRule,
                            selectedCfId = existingRule?.cfId ?: 0L
                        )
                    }

                    HipotesisGroup(
                        hipotesis = hipotesis,
                        gejalaRules = gejalaRules
                    )
                }

                _uiState.value = _uiState.value.copy(
                    hipotesisGroups = groups,
                    nilaiCfList = nilaiCfList,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat data"
                )
            }
        }
    }

    // 🔹 Perbaikan fungsi updateGejalaCfSelection
    fun updateGejalaCfSelection(hipotesisId: Long, gejalaId: Long, cfId: Long) {
        val currentGroups = _uiState.value.hipotesisGroups
        val updatedGroups = currentGroups.map { group ->
            if (group.hipotesis.id == hipotesisId) {
                group.copy(
                    gejalaRules = group.gejalaRules.map { gr ->
                        if (gr.gejala.id == gejalaId) {
                            gr.copy(selectedCfId = cfId)
                        } else gr
                    }
                )
            } else group
        }
        _uiState.value = _uiState.value.copy(hipotesisGroups = updatedGroups)
    }

    fun saveAllRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            var hasError = false
            val allGejalaRules = _uiState.value.hipotesisGroups.flatMap { it.gejalaRules }

            for (gejalaRule in allGejalaRules) {
                if (gejalaRule.selectedCfId == 0L) continue // Skip if not set

                val request = RuleRequest(
                    gejalaId = gejalaRule.gejala.id,
                    cfId = gejalaRule.selectedCfId
                )

                val result = if (gejalaRule.currentRule != null) {
                    ruleRepo.update(gejalaRule.currentRule.id, request)
                } else {
                    ruleRepo.insert(request)
                }

                if (result.isFailure) {
                    hasError = true
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message
                    )
                    break
                }
            }

            if (!hasError) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Rules berhasil disimpan"
                )
                loadAll() // Reload to get updated data
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun getNilaiCf(id: Long): NilaiCf? = _uiState.value.nilaiCfList.find { it.id == id }

    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}
