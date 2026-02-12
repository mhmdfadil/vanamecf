package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.Gejala
import com.example.cfvaname.data.GejalaHipotesis
import com.example.cfvaname.data.GejalaHipotesisRepository
import com.example.cfvaname.data.GejalaHipotesisRequest
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
    val gejalaHipotesisList: List<GejalaHipotesis> = emptyList(),
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
    val formSelectedHipotesisIds: Set<Long> = emptySet(), // Many-to-many: multiple hipotesis
    val formError: String? = null,
    val isSaving: Boolean = false
)

class GejalaViewModel : ViewModel() {

    private val repository = GejalaRepository()
    private val hipotesisRepository = HipotesisRepository()
    private val ghRepository = GejalaHipotesisRepository()

    private val _uiState = MutableStateFlow(GejalaUiState())
    val uiState: StateFlow<GejalaUiState> = _uiState.asStateFlow()

    init {
        loadHipotesisList()
        loadGejalaHipotesis()
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

    private fun loadGejalaHipotesis() {
        viewModelScope.launch {
            ghRepository.getAll().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(gejalaHipotesisList = list)
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

    /**
     * Mendapatkan daftar hipotesis yang terkait dengan gejala tertentu
     * melalui tabel pivot gejala_hipotesis
     */
    fun getHipotesisForGejala(gejalaId: Long): List<Hipotesis> {
        val ghList = _uiState.value.gejalaHipotesisList.filter { it.gejalaId == gejalaId }
        val hipIds = ghList.map { it.hipotesisId }.toSet()
        return _uiState.value.hipotesisList.filter { it.id in hipIds }
    }

    /**
     * Mendapatkan nama hipotesis pertama yang terkait (untuk kompatibilitas tampilan card)
     */
    fun getHipotesisNamaForGejala(gejalaId: Long): String {
        val hipList = getHipotesisForGejala(gejalaId)
        return if (hipList.isNotEmpty()) hipList.joinToString(", ") { it.nama } else "-"
    }

    fun getHipotesisKodeForGejala(gejalaId: Long): String {
        val hipList = getHipotesisForGejala(gejalaId)
        return if (hipList.isNotEmpty()) hipList.joinToString(", ") { it.kode } else "-"
    }

    // === ADD ===
    fun showAddDialog() {
        loadHipotesisList()
        _uiState.value = _uiState.value.copy(
            showAddDialog = true, formKode = "", formNama = "",
            formSelectedHipotesisIds = emptySet(), formError = null
        )
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, formError = null)
    }

    fun saveNewGejala() {
        val state = _uiState.value
        if (state.formKode.isBlank()) { _uiState.value = state.copy(formError = "Kode tidak boleh kosong"); return }
        if (state.formNama.isBlank()) { _uiState.value = state.copy(formError = "Nama gejala tidak boleh kosong"); return }
        if (state.formSelectedHipotesisIds.isEmpty()) { _uiState.value = state.copy(formError = "Pilih minimal 1 hipotesis"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)

            // 1. Insert gejala (tanpa hipotesis_id)
            val request = GejalaRequest(
                kode = state.formKode.trim().uppercase(),
                nama = state.formNama.trim()
            )
            repository.insert(request).fold(
                onSuccess = { newGejala ->
                    // 2. Insert relasi gejala_hipotesis untuk setiap hipotesis yang dipilih
                    var hasError = false
                    for (hipId in state.formSelectedHipotesisIds) {
                        val ghRequest = GejalaHipotesisRequest(
                            gejalaId = newGejala.id,
                            hipotesisId = hipId
                        )
                        ghRepository.insert(ghRequest).fold(
                            onSuccess = { /* ok */ },
                            onFailure = { error ->
                                hasError = true
                                _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                            }
                        )
                        if (hasError) break
                    }
                    if (!hasError) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false, showAddDialog = false,
                            successMessage = "Gejala berhasil ditambahkan"
                        )
                        loadGejalaHipotesis()
                        loadGejala()
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                }
            )
        }
    }

    // === EDIT ===
    fun showEditDialog(gejala: Gejala) {
        loadHipotesisList()
        // Load current hipotesis associations for this gejala
        val currentHipIds = _uiState.value.gejalaHipotesisList
            .filter { it.gejalaId == gejala.id }
            .map { it.hipotesisId }
            .toSet()

        _uiState.value = _uiState.value.copy(
            showEditDialog = true, selectedGejala = gejala,
            formKode = gejala.kode, formNama = gejala.nama,
            formSelectedHipotesisIds = currentHipIds, formError = null
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
        if (state.formSelectedHipotesisIds.isEmpty()) { _uiState.value = state.copy(formError = "Pilih minimal 1 hipotesis"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)

            // 1. Update gejala data
            val request = GejalaRequest(
                kode = state.formKode.trim().uppercase(),
                nama = state.formNama.trim()
            )
            repository.update(gejala.id, request).fold(
                onSuccess = {
                    // 2. Sync gejala_hipotesis: hapus yang lama, insert yang baru
                    val currentGhList = state.gejalaHipotesisList.filter { it.gejalaId == gejala.id }
                    val currentHipIds = currentGhList.map { it.hipotesisId }.toSet()
                    val newHipIds = state.formSelectedHipotesisIds

                    // Hapus relasi yang tidak ada di form
                    val toRemove = currentGhList.filter { it.hipotesisId !in newHipIds }
                    for (gh in toRemove) {
                        ghRepository.delete(gh.id)
                    }

                    // Tambah relasi baru yang belum ada
                    val toAdd = newHipIds - currentHipIds
                    var hasError = false
                    for (hipId in toAdd) {
                        val ghRequest = GejalaHipotesisRequest(
                            gejalaId = gejala.id,
                            hipotesisId = hipId
                        )
                        ghRepository.insert(ghRequest).fold(
                            onSuccess = { /* ok */ },
                            onFailure = { error ->
                                hasError = true
                                _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                            }
                        )
                        if (hasError) break
                    }

                    if (!hasError) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false, showEditDialog = false, selectedGejala = null,
                            successMessage = "Gejala berhasil diupdate"
                        )
                        loadGejalaHipotesis()
                        loadGejala()
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, formError = error.message)
                }
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
            // CASCADE akan menghapus gejala_hipotesis terkait secara otomatis
            repository.delete(gejala.id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showDeleteDialog = false, selectedGejala = null,
                        successMessage = "Gejala '${gejala.kode}' berhasil dihapus"
                    )
                    loadGejalaHipotesis()
                    loadGejala()
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
    fun onFormKodeChange(value: String) { _uiState.value = _uiState.value.copy(formKode = value, formError = null) }
    fun onFormNamaChange(value: String) { _uiState.value = _uiState.value.copy(formNama = value, formError = null) }

    /**
     * Toggle hipotesis selection (many-to-many)
     */
    fun toggleHipotesisSelection(hipotesisId: Long) {
        val current = _uiState.value.formSelectedHipotesisIds.toMutableSet()
        if (hipotesisId in current) {
            current.remove(hipotesisId)
        } else {
            current.add(hipotesisId)
        }
        _uiState.value = _uiState.value.copy(formSelectedHipotesisIds = current, formError = null)
    }

    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}