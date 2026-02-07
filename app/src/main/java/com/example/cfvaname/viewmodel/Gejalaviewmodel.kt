package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.Gejala
import com.example.cfvaname.data.GejalaRepository
import com.example.cfvaname.data.GejalaRequest
import com.example.cfvaname.data.Hipotesis
import com.example.cfvaname.data.HipotesisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GejalaUiState(
    val gejalaList: List<Gejala> = emptyList(),
    val hipotesisList: List<Hipotesis> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    // Dialog states
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedGejala: Gejala? = null,
    // Form fields
    val formKode: String = "",
    val formNama: String = "",
    val formHipotesisId: Long = 0,
    val formError: String? = null,
    val isSaving: Boolean = false
)

class GejalaViewModel : ViewModel() {

    private val repository = GejalaRepository()
    private val hipotesisRepository = HipotesisRepository()

    private val _uiState = MutableStateFlow(GejalaUiState())
    val uiState: StateFlow<GejalaUiState> = _uiState.asStateFlow()

    init {
        loadHipotesisList()
        loadGejala()
    }

    private fun loadHipotesisList() {
        viewModelScope.launch {
            hipotesisRepository.getAll().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(hipotesisList = list)
                },
                onFailure = { /* silent */ }
            )
        }
    }

    fun loadGejala() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val query = _uiState.value.searchQuery
            val result = if (query.isBlank()) repository.getAll() else repository.search(query)
            result.fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(gejalaList = list, isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadGejala()
    }

    fun getHipotesisNama(hipotesisId: Long): String {
        return _uiState.value.hipotesisList.find { it.id == hipotesisId }?.nama ?: "ID: $hipotesisId"
    }

    fun getHipotesisKode(hipotesisId: Long): String {
        return _uiState.value.hipotesisList.find { it.id == hipotesisId }?.kode ?: "-"
    }

    // === ADD ===
    fun showAddDialog() {
        loadHipotesisList()
        _uiState.value = _uiState.value.copy(
            showAddDialog = true, formKode = "", formNama = "", formHipotesisId = 0, formError = null
        )
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, formError = null)
    }

    fun saveNewGejala() {
        val state = _uiState.value
        if (state.formKode.isBlank()) { _uiState.value = state.copy(formError = "Kode tidak boleh kosong"); return }
        if (state.formNama.isBlank()) { _uiState.value = state.copy(formError = "Nama gejala tidak boleh kosong"); return }
        if (state.formHipotesisId == 0L) { _uiState.value = state.copy(formError = "Pilih hipotesis terlebih dahulu"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            val request = GejalaRequest(kode = state.formKode.trim().uppercase(), nama = state.formNama.trim(), hipotesisId = state.formHipotesisId)
            repository.insert(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, showAddDialog = false, successMessage = "Gejala berhasil ditambahkan")
                    loadGejala()
                },
                onFailure = { error -> _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message) }
            )
        }
    }

    // === EDIT ===
    fun showEditDialog(gejala: Gejala) {
        loadHipotesisList()
        _uiState.value = _uiState.value.copy(
            showEditDialog = true, selectedGejala = gejala,
            formKode = gejala.kode, formNama = gejala.nama, formHipotesisId = gejala.hipotesisId, formError = null
        )
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, selectedGejala = null, formError = null)
    }

    fun saveEditGejala() {
        val state = _uiState.value
        val gejala = state.selectedGejala ?: return
        if (state.formKode.isBlank()) { _uiState.value = state.copy(formError = "Kode tidak boleh kosong"); return }
        if (state.formNama.isBlank()) { _uiState.value = state.copy(formError = "Nama gejala tidak boleh kosong"); return }
        if (state.formHipotesisId == 0L) { _uiState.value = state.copy(formError = "Pilih hipotesis terlebih dahulu"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            val request = GejalaRequest(kode = state.formKode.trim().uppercase(), nama = state.formNama.trim(), hipotesisId = state.formHipotesisId)
            repository.update(gejala.id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, showEditDialog = false, selectedGejala = null, successMessage = "Gejala berhasil diupdate")
                    loadGejala()
                },
                onFailure = { error -> _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message) }
            )
        }
    }

    // === DELETE ===
    fun showDeleteDialog(gejala: Gejala) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true, selectedGejala = gejala)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false, selectedGejala = null)
    }

    fun confirmDelete() {
        val gejala = _uiState.value.selectedGejala ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            repository.delete(gejala.id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, showDeleteDialog = false, selectedGejala = null, successMessage = "Gejala '${gejala.kode}' berhasil dihapus")
                    loadGejala()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, showDeleteDialog = false, errorMessage = error.message)
                }
            )
        }
    }

    // === FORM FIELDS ===
    fun onFormKodeChange(value: String) { _uiState.value = _uiState.value.copy(formKode = value, formError = null) }
    fun onFormNamaChange(value: String) { _uiState.value = _uiState.value.copy(formNama = value, formError = null) }
    fun onFormHipotesisIdChange(value: Long) { _uiState.value = _uiState.value.copy(formHipotesisId = value, formError = null) }
    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}