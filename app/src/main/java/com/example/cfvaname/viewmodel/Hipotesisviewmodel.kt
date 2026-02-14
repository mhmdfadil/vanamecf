package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.AutoRefreshManager
import com.example.cfvaname.data.Hipotesis
import com.example.cfvaname.data.HipotesisRepository
import com.example.cfvaname.data.HipotesisRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HipotesisUiState(
    val hipotesisList: List<Hipotesis> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    // Dialog states
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showDetailDialog: Boolean = false,
    val selectedHipotesis: Hipotesis? = null,
    // Form fields
    val formKode: String = "",
    val formNama: String = "",
    val formDeskripsi: String = "",
    val formRekomendasi: String = "",
    val formError: String? = null,
    val isSaving: Boolean = false
)

class HipotesisViewModel : ViewModel() {

    private val repository = HipotesisRepository()

    private val _uiState = MutableStateFlow(HipotesisUiState())
    val uiState: StateFlow<HipotesisUiState> = _uiState.asStateFlow()

    init {
        loadHipotesis()
        // ✅ Auto-refresh global dengan deteksi perubahan
        viewModelScope.launch {
            AutoRefreshManager.refreshTick.collect { loadHipotesis() }
        }
    }

    fun loadHipotesis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val query = _uiState.value.searchQuery
            val result = if (query.isBlank()) {
                repository.getAll()
            } else {
                repository.search(query)
            }
            result.fold(
                onSuccess = { list ->
                    // ✅ Deteksi perubahan hipotesis
                    val checksum = AutoRefreshManager.calculateChecksum(list)
                    if (AutoRefreshManager.hasChanged("hipotesis_list_${query}", checksum)) {
                        _uiState.value = _uiState.value.copy(hipotesisList = list, isLoading = false)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadHipotesis()
    }

    // === DETAIL DIALOG ===
    fun showDetailDialog(hipotesis: Hipotesis) {
        _uiState.value = _uiState.value.copy(showDetailDialog = true, selectedHipotesis = hipotesis)
    }

    fun hideDetailDialog() {
        _uiState.value = _uiState.value.copy(showDetailDialog = false, selectedHipotesis = null)
    }

    // === ADD DIALOG ===
    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true,
            formKode = "", formNama = "", formDeskripsi = "", formRekomendasi = "",
            formError = null
        )
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, formError = null)
    }

    fun saveNewHipotesis() {
        val state = _uiState.value
        if (state.formKode.isBlank()) {
            _uiState.value = state.copy(formError = "Kode tidak boleh kosong"); return
        }
        if (state.formNama.isBlank()) {
            _uiState.value = state.copy(formError = "Nama hipotesis tidak boleh kosong"); return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            val request = HipotesisRequest(
                kode = state.formKode.trim().uppercase(),
                nama = state.formNama.trim(),
                deskripsi = state.formDeskripsi.trim().ifBlank { null },
                rekomendasi = state.formRekomendasi.trim().ifBlank { null }
            )
            repository.insert(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showAddDialog = false,
                        successMessage = "Hipotesis berhasil ditambahkan"
                    )
                    // ✅ Invalidate cache dan trigger refresh
                    AutoRefreshManager.invalidateAndRefresh(
                        "hipotesis_list_",
                        "gejala_hipotesis_list",
                        "dashboard_data"
                    )
                    loadHipotesis()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                }
            )
        }
    }

    // === EDIT DIALOG ===
    fun showEditDialog(hipotesis: Hipotesis) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            selectedHipotesis = hipotesis,
            formKode = hipotesis.kode,
            formNama = hipotesis.nama,
            formDeskripsi = hipotesis.deskripsi ?: "",
            formRekomendasi = hipotesis.rekomendasi ?: "",
            formError = null
        )
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, selectedHipotesis = null, formError = null)
    }

    fun saveEditHipotesis() {
        val state = _uiState.value
        val hipotesis = state.selectedHipotesis ?: return
        if (state.formKode.isBlank()) {
            _uiState.value = state.copy(formError = "Kode tidak boleh kosong"); return
        }
        if (state.formNama.isBlank()) {
            _uiState.value = state.copy(formError = "Nama hipotesis tidak boleh kosong"); return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            val request = HipotesisRequest(
                kode = state.formKode.trim().uppercase(),
                nama = state.formNama.trim(),
                deskripsi = state.formDeskripsi.trim().ifBlank { null },
                rekomendasi = state.formRekomendasi.trim().ifBlank { null }
            )
            repository.update(hipotesis.id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showEditDialog = false, selectedHipotesis = null,
                        successMessage = "Hipotesis berhasil diupdate"
                    )
                    // ✅ Invalidate cache dan trigger refresh
                    AutoRefreshManager.invalidateAndRefresh(
                        "hipotesis_list_",
                        "gejala_hipotesis_list",
                        "dashboard_data"
                    )
                    loadHipotesis()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                }
            )
        }
    }

    // === DELETE DIALOG ===
    fun showDeleteDialog(hipotesis: Hipotesis) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true, selectedHipotesis = hipotesis)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false, selectedHipotesis = null)
    }

    fun confirmDelete() {
        val hipotesis = _uiState.value.selectedHipotesis ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            repository.delete(hipotesis.id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showDeleteDialog = false, selectedHipotesis = null,
                        successMessage = "Hipotesis '${hipotesis.kode}' berhasil dihapus"
                    )
                    // ✅ Invalidate cache dan trigger refresh
                    AutoRefreshManager.invalidateAndRefresh(
                        "hipotesis_list_",
                        "gejala_hipotesis_list",
                        "dashboard_data"
                    )
                    loadHipotesis()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showDeleteDialog = false, errorMessage = error.message
                    )
                }
            )
        }
    }

    // === FORM FIELDS ===
    fun onFormKodeChange(v: String) { _uiState.value = _uiState.value.copy(formKode = v, formError = null) }
    fun onFormNamaChange(v: String) { _uiState.value = _uiState.value.copy(formNama = v, formError = null) }
    fun onFormDeskripsiChange(v: String) { _uiState.value = _uiState.value.copy(formDeskripsi = v, formError = null) }
    fun onFormRekomendasiChange(v: String) { _uiState.value = _uiState.value.copy(formRekomendasi = v, formError = null) }
    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}