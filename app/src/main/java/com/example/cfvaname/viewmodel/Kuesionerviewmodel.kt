package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.AutoRefreshManager
import com.example.cfvaname.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KuesionerSummary(
    val kuesioner: Kuesioner,
    val gejalaCount: Int = 0
)

data class KuesionerUiState(
    val summaryList: List<KuesionerSummary> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCreateForm: Boolean = false,
    val formNama: String = "",
    val formNoHp: String = "",
    val formLokasi: String = "",
    val formUsiaUdang: String = "",
    val formError: String? = null,
    val isSaving: Boolean = false,
    val gejalaList: List<Gejala> = emptyList(),
    val hipotesisList: List<Hipotesis> = emptyList(),
    val gejalaHipotesisList: List<GejalaHipotesis> = emptyList(),
    val nilaiCfList: List<NilaiCf> = emptyList(),
    val selectedGejalaCf: Map<Long, Long> = emptyMap(),
    val showHasil: Boolean = false,
    val hasilKuesioner: Kuesioner? = null,
    val hasilResults: List<HipotesisResult> = emptyList(),
    val hasilKuesionerDataList: List<KuesionerData> = emptyList(),
    val allRules: List<Rule> = emptyList(),
    val allNilaiCfMap: Map<Long, NilaiCf> = emptyMap(),
    val allGejalaMap: Map<Long, Gejala> = emptyMap(),
    val allGejalaHipotesisMap: Map<Long, GejalaHipotesis> = emptyMap(),
    val showDeleteDialog: Boolean = false,
    val selectedKuesioner: Kuesioner? = null
)

class KuesionerViewModel : ViewModel() {

    private val repo = KuesionerRepository()

    private val _uiState = MutableStateFlow(KuesionerUiState())
    val uiState: StateFlow<KuesionerUiState> = _uiState.asStateFlow()

    init {
        loadList()
        // ✅ Auto-refresh global dengan deteksi perubahan
        viewModelScope.launch {
            AutoRefreshManager.refreshTick.collect { loadList() }
        }
    }

    fun loadList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repo.getAllKuesioner().fold(
                onSuccess = { list ->
                    val summaries = list.map { k ->
                        KuesionerSummary(kuesioner = k, gejalaCount = repo.countKuesionerData(k.id))
                    }
                    
                    // ✅ Deteksi perubahan kuesioner list
                    val checksum = AutoRefreshManager.calculateChecksum(summaries.map { it.kuesioner })
                    if (AutoRefreshManager.hasChanged("kuesioner_list", checksum)) {
                        _uiState.value = _uiState.value.copy(summaryList = summaries, isLoading = false)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
            )
        }
    }

    // ===================== CREATE FORM =====================

    fun showCreateForm() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showCreateForm = true, showHasil = false,
                formNama = "", formNoHp = "", formLokasi = "", formUsiaUdang = "",
                selectedGejalaCf = emptyMap(), formError = null
            )
            repo.getAllGejalas().onSuccess { _uiState.value = _uiState.value.copy(gejalaList = it) }
            repo.getAllHipotesis().onSuccess { _uiState.value = _uiState.value.copy(hipotesisList = it) }
            repo.getAllGejalaHipotesis().onSuccess { _uiState.value = _uiState.value.copy(gejalaHipotesisList = it) }
            repo.getAllNilaiCf().onSuccess { _uiState.value = _uiState.value.copy(nilaiCfList = it) }
        }
    }

    fun hideCreateForm() {
        _uiState.value = _uiState.value.copy(showCreateForm = false, formError = null)
    }

    fun onFormNamaChange(v: String) { _uiState.value = _uiState.value.copy(formNama = v, formError = null) }
    fun onFormNoHpChange(v: String) { _uiState.value = _uiState.value.copy(formNoHp = v, formError = null) }
    fun onFormLokasiChange(v: String) { _uiState.value = _uiState.value.copy(formLokasi = v, formError = null) }
    fun onFormUsiaUdangChange(v: String) { _uiState.value = _uiState.value.copy(formUsiaUdang = v, formError = null) }

    fun toggleGejala(gejalaId: Long, nilaiCfId: Long) {
        val map = _uiState.value.selectedGejalaCf.toMutableMap()
        if (nilaiCfId == 0L) {
            map.remove(gejalaId)
        } else {
            map[gejalaId] = nilaiCfId
        }
        _uiState.value = _uiState.value.copy(selectedGejalaCf = map, formError = null)
    }

    fun removeGejala(gejalaId: Long) {
        val map = _uiState.value.selectedGejalaCf.toMutableMap()
        map.remove(gejalaId)
        _uiState.value = _uiState.value.copy(selectedGejalaCf = map)
    }

    fun submitKuesioner() {
        val s = _uiState.value
        if (s.formNama.isBlank()) { _uiState.value = s.copy(formError = "Nama petambak wajib diisi"); return }
        if (s.formNoHp.isBlank()) { _uiState.value = s.copy(formError = "No. HP wajib diisi"); return }
        if (s.formLokasi.isBlank()) { _uiState.value = s.copy(formError = "Lokasi tambak wajib diisi"); return }
        val usia = s.formUsiaUdang.toIntOrNull()
        if (usia == null || usia <= 0) { _uiState.value = s.copy(formError = "Usia udang harus angka > 0"); return }
        if (s.selectedGejalaCf.isEmpty()) { _uiState.value = s.copy(formError = "Pilih minimal 1 gejala"); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, formError = null)

            val kuesionerReq = KuesionerInsertRequest(
                namaPetambak = s.formNama.trim(),
                noHp = s.formNoHp.trim(),
                lokasiTambak = s.formLokasi.trim(),
                usiaUdang = usia
            )

            repo.insertKuesioner(kuesionerReq).fold(
                onSuccess = { kuesioner ->
                    val ghMap = s.gejalaHipotesisList.groupBy { it.gejalaId }
                    val dataList = s.selectedGejalaCf.mapNotNull { (gejalaId, cfId) ->
                        val firstGh = ghMap[gejalaId]?.firstOrNull()
                        if (firstGh != null) {
                            KuesionerDataInsertRequest(
                                kuesionerId = kuesioner.id,
                                gejalaHipotesisId = firstGh.id,
                                cfValue = cfId
                            )
                        } else {
                            null
                        }
                    }

                    repo.insertKuesionerData(dataList).fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false, showCreateForm = false,
                                successMessage = "Kuesioner berhasil disimpan"
                            )
                            // ✅ Invalidate cache dan trigger refresh
                            AutoRefreshManager.invalidateAndRefresh(
                                "kuesioner_list",
                                "dashboard_data"
                            )
                            loadList()
                            showHasil(kuesioner)
                        },
                        onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, formError = it.message) }
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, formError = it.message) }
            )
        }
    }

    // ===================== HASIL / RESULT =====================

    fun showHasil(kuesioner: Kuesioner) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showHasil = true, showCreateForm = false,
                hasilKuesioner = kuesioner, isLoading = true
            )

            val kuesionerDataResult = repo.getKuesionerData(kuesioner.id)
            val gejalaResult = repo.getAllGejalas()
            val hipotesisResult = repo.getAllHipotesis()
            val ghResult = repo.getAllGejalaHipotesis()
            val rulesResult = repo.getAllRules()
            val nilaiCfResult = repo.getAllNilaiCf()

            val kuesionerDataList = kuesionerDataResult.getOrDefault(emptyList())
            val gejalaList = gejalaResult.getOrDefault(emptyList())
            val hipotesisList = hipotesisResult.getOrDefault(emptyList())
            val ghList = ghResult.getOrDefault(emptyList())
            val rulesList = rulesResult.getOrDefault(emptyList())
            val nilaiCfList = nilaiCfResult.getOrDefault(emptyList())

            val nilaiCfMap = nilaiCfList.associateBy { it.id }
            val gejalaMap = gejalaList.associateBy { it.id }
            val ghMap = ghList.associateBy { it.id }
            
            val ghByGejalaId = ghList.groupBy { it.gejalaId }
            
            val gejalaIdToCfId = mutableMapOf<Long, Long>()
            for (data in kuesionerDataList) {
                val gh = ghMap[data.gejalaHipotesisId]
                if (gh != null) {
                    gejalaIdToCfId[gh.gejalaId] = data.cfValue
                }
            }

            val results = mutableListOf<HipotesisResult>()

            for (hipotesis in hipotesisList) {
                val ghForHipotesis = ghList.filter { it.hipotesisId == hipotesis.id }

                var cfCombine = 0.0
                var prevCF = 0.0
                var count = 0
                val steps = mutableListOf<CfCalculationStep>()
                val processedGejalaIds = mutableSetOf<Long>()

                for (gh in ghForHipotesis) {
                    val gejalaId = gh.gejalaId
                    val gejala = gejalaMap[gejalaId] ?: continue

                    if (processedGejalaIds.contains(gejalaId)) continue

                    val cfUser = gejalaIdToCfId[gejalaId]
                    if (cfUser == null || cfUser == 0L) continue

                    processedGejalaIds.add(gejalaId)

                    val rule = rulesList.find { it.gejalaHipotesisId == gh.id }
                    val cfPakar = if (rule != null) nilaiCfMap[rule.cfId]?.nilai ?: 0.0 else 0.0

                    val cfUserValue = nilaiCfMap[cfUser]?.nilai ?: 0.0

                    val cfGejala = cfPakar * cfUserValue

                    val cfSebelum = prevCF
                    val cfSesudah: Double

                    if (count == 0) {
                        cfCombine = cfGejala
                        cfSesudah = cfGejala
                    } else {
                        cfCombine = prevCF + cfGejala * (1 - prevCF)
                        cfSesudah = cfCombine
                    }

                    steps.add(
                        CfCalculationStep(
                            gejalaKode = gejala.kode, gejalaNama = gejala.nama,
                            cfPakar = cfPakar, cfUser = cfUserValue, cfGejala = cfGejala,
                            cfSebelum = cfSebelum, cfSesudah = cfSesudah
                        )
                    )

                    prevCF = cfCombine
                    count++
                }

                if (count > 0) {
                    results.add(
                        HipotesisResult(
                            hipotesis = hipotesis, cfCombine = cfCombine,
                            percentage = Math.round(cfCombine * 10000.0) / 100.0,
                            steps = steps
                        )
                    )
                }
            }

            results.sortByDescending { it.cfCombine }

            _uiState.value = _uiState.value.copy(
                isLoading = false, hasilResults = results,
                hasilKuesionerDataList = kuesionerDataList,
                allRules = rulesList, allNilaiCfMap = nilaiCfMap,
                allGejalaMap = gejalaMap, allGejalaHipotesisMap = ghMap
            )
        }
    }

    fun hideHasil() {
        _uiState.value = _uiState.value.copy(
            showHasil = false, hasilKuesioner = null, hasilResults = emptyList()
        )
    }

    // ===================== DELETE =====================

    fun showDeleteDialog(kuesioner: Kuesioner) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true, selectedKuesioner = kuesioner)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false, selectedKuesioner = null)
    }

    fun confirmDelete() {
        val k = _uiState.value.selectedKuesioner ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            repo.deleteKuesioner(k.id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showDeleteDialog = false, selectedKuesioner = null,
                        successMessage = "Kuesioner berhasil dihapus"
                    )
                    // ✅ Invalidate cache dan trigger refresh
                    AutoRefreshManager.invalidateAndRefresh(
                        "kuesioner_list",
                        "dashboard_data"
                    )
                    loadList()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, showDeleteDialog = false, errorMessage = it.message
                    )
                }
            )
        }
    }

    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}