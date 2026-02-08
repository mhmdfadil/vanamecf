package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RuleUiState(
    val ruleList: List<Rule> = emptyList(),
    val gejalaList: List<Gejala> = emptyList(),
    val nilaiCfList: List<NilaiCf> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Dialog
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedRule: Rule? = null,
    // Form
    val formGejalaId: Long = 0,
    val formCfId: Long = 0,
    val formError: String? = null,
    val isSaving: Boolean = false
)

class RuleViewModel : ViewModel() {

    private val ruleRepo = RuleRepository()
    private val gejalaRepo = GejalaRepository()
    private val nilaiCfRepo = NilaiCfRepository()

    private val _uiState = MutableStateFlow(RuleUiState())
    val uiState: StateFlow<RuleUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            // Load all 3 in parallel-ish
            val gejalaResult = gejalaRepo.getAll()
            val nilaiCfResult = nilaiCfRepo.getAll()
            val ruleResult = ruleRepo.getAll()

            gejalaResult.onSuccess { _uiState.value = _uiState.value.copy(gejalaList = it) }
            nilaiCfResult.onSuccess { _uiState.value = _uiState.value.copy(nilaiCfList = it) }
            ruleResult.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(ruleList = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
            )
        }
    }

    fun loadRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            ruleRepo.getAll().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(ruleList = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
            )
        }
    }

    fun getGejala(id: Long): Gejala? = _uiState.value.gejalaList.find { it.id == id }
    fun getNilaiCf(id: Long): NilaiCf? = _uiState.value.nilaiCfList.find { it.id == id }

    // Gejala yang belum punya rule (untuk add), kecuali yang sedang diedit
    fun getAvailableGejala(excludeGejalaId: Long? = null): List<Gejala> {
        val usedIds = _uiState.value.ruleList.map { it.gejalaId }.toSet()
        return _uiState.value.gejalaList.filter { it.id !in usedIds || it.id == excludeGejalaId }
    }

    // === ADD ===
    fun showAddDialog() {
        // Refresh dropdown data
        viewModelScope.launch {
            gejalaRepo.getAll().onSuccess { _uiState.value = _uiState.value.copy(gejalaList = it) }
            nilaiCfRepo.getAll().onSuccess { _uiState.value = _uiState.value.copy(nilaiCfList = it) }
        }
        _uiState.value = _uiState.value.copy(showAddDialog = true, formGejalaId = 0, formCfId = 0, formError = null)
    }

    fun hideAddDialog() { _uiState.value = _uiState.value.copy(showAddDialog = false, formError = null) }

    fun saveNew() {
        val s = _uiState.value
        if (s.formGejalaId == 0L) { _uiState.value = s.copy(formError = "Pilih gejala terlebih dahulu"); return }
        if (s.formCfId == 0L) { _uiState.value = s.copy(formError = "Pilih nilai CF terlebih dahulu"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            ruleRepo.insert(RuleRequest(gejalaId = s.formGejalaId, cfId = s.formCfId)).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, showAddDialog = false, successMessage = "Rule berhasil ditambahkan")
                    loadRules()
                },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, formError = it.message) }
            )
        }
    }

    // === EDIT ===
    fun showEditDialog(rule: Rule) {
        viewModelScope.launch {
            gejalaRepo.getAll().onSuccess { _uiState.value = _uiState.value.copy(gejalaList = it) }
            nilaiCfRepo.getAll().onSuccess { _uiState.value = _uiState.value.copy(nilaiCfList = it) }
        }
        _uiState.value = _uiState.value.copy(
            showEditDialog = true, selectedRule = rule,
            formGejalaId = rule.gejalaId, formCfId = rule.cfId, formError = null
        )
    }

    fun hideEditDialog() { _uiState.value = _uiState.value.copy(showEditDialog = false, selectedRule = null, formError = null) }

    fun saveEdit() {
        val s = _uiState.value
        val rule = s.selectedRule ?: return
        if (s.formGejalaId == 0L) { _uiState.value = s.copy(formError = "Pilih gejala"); return }
        if (s.formCfId == 0L) { _uiState.value = s.copy(formError = "Pilih nilai CF"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            ruleRepo.update(rule.id, RuleRequest(gejalaId = s.formGejalaId, cfId = s.formCfId)).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, showEditDialog = false, selectedRule = null, successMessage = "Rule berhasil diupdate")
                    loadRules()
                },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, formError = it.message) }
            )
        }
    }

    // === DELETE ===
    fun showDeleteDialog(rule: Rule) { _uiState.value = _uiState.value.copy(showDeleteDialog = true, selectedRule = rule) }
    fun hideDeleteDialog() { _uiState.value = _uiState.value.copy(showDeleteDialog = false, selectedRule = null) }

    fun confirmDelete() {
        val rule = _uiState.value.selectedRule ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            ruleRepo.delete(rule.id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, showDeleteDialog = false, selectedRule = null, successMessage = "Rule berhasil dihapus")
                    loadRules()
                },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, showDeleteDialog = false, errorMessage = it.message) }
            )
        }
    }

    // === FORM ===
    fun onFormGejalaIdChange(v: Long) { _uiState.value = _uiState.value.copy(formGejalaId = v, formError = null) }
    fun onFormCfIdChange(v: Long) { _uiState.value = _uiState.value.copy(formCfId = v, formError = null) }
    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}