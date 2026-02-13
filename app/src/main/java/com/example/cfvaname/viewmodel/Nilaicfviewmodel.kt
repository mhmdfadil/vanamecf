package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.AutoRefreshManager
import com.example.cfvaname.data.NilaiCf
import com.example.cfvaname.data.NilaiCfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NilaiCfUiState(
    val nilaiCfList: List<NilaiCf> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Dialog states
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedNilaiCf: NilaiCf? = null,
    // Form
    val formKeterangan: String = "",
    val formError: String? = null,
    val isSaving: Boolean = false
)

class NilaiCfViewModel : ViewModel() {

    private val repository = NilaiCfRepository()

    private val _uiState = MutableStateFlow(NilaiCfUiState())
    val uiState: StateFlow<NilaiCfUiState> = _uiState.asStateFlow()

    init {
        loadData()
        // ✅ Auto-refresh global
        viewModelScope.launch {
            AutoRefreshManager.refreshTick.collect { loadData() }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getAll().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(nilaiCfList = list, isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
            )
        }
    }

    /**
     * Preview: menghitung bagaimana nilai akan terdistribusi
     * jika ada N+1 item (setelah tambah 1)
     */
    fun previewDistribution(currentCount: Int): List<Double> {
        val newCount = currentCount + 1
        if (newCount <= 1) return listOf(1.0)
        return (0 until newCount).map { i ->
            (i.toDouble() / (newCount - 1)).coerceAtMost(1.0)
        }.map { Math.round(it * 100.0) / 100.0 }
    }

    // === ADD ===
    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, formKeterangan = "", formError = null)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, formError = null)
    }

    fun saveNew() {
        val state = _uiState.value
        if (state.formKeterangan.isBlank()) {
            _uiState.value = state.copy(formError = "Keterangan tidak boleh kosong"); return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            repository.insert(state.formKeterangan).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showAddDialog = false,
                        successMessage = "Nilai CF berhasil ditambahkan (nilai diperbarui otomatis)"
                    )
                    loadData()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                }
            )
        }
    }

    // === EDIT (keterangan only) ===
    fun showEditDialog(nilaiCf: NilaiCf) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true, selectedNilaiCf = nilaiCf,
            formKeterangan = nilaiCf.keterangan, formError = null
        )
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, selectedNilaiCf = null, formError = null)
    }

    fun saveEdit() {
        val state = _uiState.value
        val item = state.selectedNilaiCf ?: return
        if (state.formKeterangan.isBlank()) {
            _uiState.value = state.copy(formError = "Keterangan tidak boleh kosong"); return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)
            repository.updateKeterangan(item.id, state.formKeterangan).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showEditDialog = false, selectedNilaiCf = null,
                        successMessage = "Keterangan berhasil diupdate"
                    )
                    loadData()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                }
            )
        }
    }

    // === DELETE ===
    fun showDeleteDialog(nilaiCf: NilaiCf) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true, selectedNilaiCf = nilaiCf)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false, selectedNilaiCf = null)
    }

    fun confirmDelete() {
        val item = _uiState.value.selectedNilaiCf ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            repository.delete(item.id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showDeleteDialog = false, selectedNilaiCf = null,
                        successMessage = "Nilai CF '${item.keterangan}' berhasil dihapus (nilai diperbarui otomatis)"
                    )
                    loadData()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, showDeleteDialog = false, errorMessage = error.message)
                }
            )
        }
    }

    // === FORM ===
    fun onFormKeteranganChange(v: String) { _uiState.value = _uiState.value.copy(formKeterangan = v, formError = null) }
    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}