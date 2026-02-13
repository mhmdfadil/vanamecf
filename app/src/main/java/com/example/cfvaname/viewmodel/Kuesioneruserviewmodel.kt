package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KuesionerUserUiState(
    // List gejala unik (30 gejala)
    val uniqueGejalaList: List<Gejala> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Form fields
    val formNama: String = "",
    val formNoHp: String = "",
    val formLokasi: String = "",
    val formUsiaUdang: String = "",
    val formError: String? = null,
    val isSaving: Boolean = false,
    
    // Data untuk perhitungan CF
    val hipotesisList: List<Hipotesis> = emptyList(),
    val gejalaHipotesisList: List<GejalaHipotesis> = emptyList(),
    val nilaiCfList: List<NilaiCf> = emptyList(),
    
    // Map: gejalaId -> nilaiCfId (user pilih gejala berdasarkan gejalaId)
    val selectedGejalaCf: Map<Long, Long> = emptyMap(),
    
    // Hasil screen
    val showHasil: Boolean = false,
    val hasilKuesioner: Kuesioner? = null,
    val hasilResults: List<HipotesisResult> = emptyList(),
    val allRules: List<Rule> = emptyList(),
    val allNilaiCfMap: Map<Long, NilaiCf> = emptyMap(),
    val allGejalaMap: Map<Long, Gejala> = emptyMap()
)

class KuesionerUserViewModel : ViewModel() {

    private val repo = KuesionerRepository()

    private val _uiState = MutableStateFlow(KuesionerUserUiState())
    val uiState: StateFlow<KuesionerUserUiState> = _uiState.asStateFlow()

    init {
        loadFormData()
    }

    private fun loadFormData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load semua data
            val gejalaResult = repo.getAllGejalas()
            val hipotesisResult = repo.getAllHipotesis()
            val ghResult = repo.getAllGejalaHipotesis()
            val nilaiCfResult = repo.getAllNilaiCf()

            val allGejala = gejalaResult.getOrDefault(emptyList())
            val hipotesisList = hipotesisResult.getOrDefault(emptyList())
            val ghList = ghResult.getOrDefault(emptyList())
            val nilaiCfList = nilaiCfResult.getOrDefault(emptyList())

            // Ambil gejala unik (30 gejala) - group by gejalaId dari gejala_hipotesis
            val uniqueGejalaIds = ghList.map { it.gejalaId }.distinct()
            val uniqueGejalaList = allGejala.filter { it.id in uniqueGejalaIds }
                .sortedBy { it.kode }

            _uiState.value = _uiState.value.copy(
                uniqueGejalaList = uniqueGejalaList,
                hipotesisList = hipotesisList,
                gejalaHipotesisList = ghList,
                nilaiCfList = nilaiCfList,
                isLoading = false
            )
        }
    }

    // === FORM HANDLERS ===
    fun onFormNamaChange(v: String) { 
        _uiState.value = _uiState.value.copy(formNama = v, formError = null) 
    }
    
    fun onFormNoHpChange(v: String) { 
        _uiState.value = _uiState.value.copy(formNoHp = v, formError = null) 
    }
    
    fun onFormLokasiChange(v: String) { 
        _uiState.value = _uiState.value.copy(formLokasi = v, formError = null) 
    }
    
    fun onFormUsiaUdangChange(v: String) { 
        _uiState.value = _uiState.value.copy(formUsiaUdang = v, formError = null) 
    }

    /**
     * Toggle gejala berdasarkan gejalaId
     * User memilih dari 30 gejala unik
     */
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
        if (s.formNama.isBlank()) { 
            _uiState.value = s.copy(formError = "Nama petambak wajib diisi")
            return 
        }
        if (s.formNoHp.isBlank()) { 
            _uiState.value = s.copy(formError = "No. HP wajib diisi")
            return 
        }
        if (s.formLokasi.isBlank()) { 
            _uiState.value = s.copy(formError = "Lokasi tambak wajib diisi")
            return 
        }
        val usia = s.formUsiaUdang.toIntOrNull()
        if (usia == null || usia <= 0) { 
            _uiState.value = s.copy(formError = "Usia udang harus angka > 0")
            return 
        }
        if (s.selectedGejalaCf.isEmpty()) { 
            _uiState.value = s.copy(formError = "Pilih minimal 1 gejala")
            return 
        }

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
                    // ✅ PENTING: Insert 39 data (semua gejala_hipotesis untuk gejala yang dipilih)
                    // User pilih 30 gejala unik, tapi kita save semua kombinasi gejala_hipotesis (39 entries)
                    val dataList = mutableListOf<KuesionerDataInsertRequest>()
                    
                    s.selectedGejalaCf.forEach { (gejalaId, cfId) ->
                        // Ambil SEMUA gejala_hipotesis untuk gejala ini
                        val ghForGejala = s.gejalaHipotesisList.filter { it.gejalaId == gejalaId }
                        
                        // Insert untuk setiap kombinasi gejala-hipotesis
                        ghForGejala.forEach { gh ->
                            dataList.add(
                                KuesionerDataInsertRequest(
                                    kuesionerId = kuesioner.id,
                                    gejalaHipotesisId = gh.id,
                                    cfValue = cfId  // CF user sama untuk semua hipotesis
                                )
                            )
                        }
                    }

                    repo.insertKuesionerData(dataList).fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                successMessage = "Kuesioner berhasil disimpan"
                            )
                            // Langsung tampilkan hasil
                            showHasil(kuesioner)
                        },
                        onFailure = { 
                            _uiState.value = _uiState.value.copy(
                                isSaving = false, 
                                formError = it.message
                            ) 
                        }
                    )
                },
                onFailure = { 
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, 
                        formError = it.message
                    ) 
                }
            )
        }
    }

    // === HASIL / RESULT ===
    fun showHasil(kuesioner: Kuesioner) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showHasil = true,
                hasilKuesioner = kuesioner,
                isLoading = true
            )

            // Load data untuk perhitungan
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
            
            // Map gejalaId -> nilaiCfId
            val gejalaIdToCfId = mutableMapOf<Long, Long>()
            for (data in kuesionerDataList) {
                val gh = ghMap[data.gejalaHipotesisId]
                if (gh != null) {
                    gejalaIdToCfId[gh.gejalaId] = data.cfValue
                }
            }

            // CF Calculation per hipotesis
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

                    // CF Pakar dari rules
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
                            gejalaKode = gejala.kode,
                            gejalaNama = gejala.nama,
                            cfPakar = cfPakar,
                            cfUser = cfUserValue,
                            cfGejala = cfGejala,
                            cfSebelum = cfSebelum,
                            cfSesudah = cfSesudah
                        )
                    )

                    prevCF = cfCombine
                    count++
                }

                if (count > 0) {
                    results.add(
                        HipotesisResult(
                            hipotesis = hipotesis,
                            cfCombine = cfCombine,
                            percentage = Math.round(cfCombine * 10000.0) / 100.0,
                            steps = steps
                        )
                    )
                }
            }

            results.sortByDescending { it.cfCombine }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasilResults = results,
                allRules = rulesList,
                allNilaiCfMap = nilaiCfMap,
                allGejalaMap = gejalaMap
            )
        }
    }

    fun hideHasil() {
        _uiState.value = _uiState.value.copy(
            showHasil = false,
            hasilKuesioner = null,
            hasilResults = emptyList()
        )
    }

    fun clearSuccessMessage() { 
        _uiState.value = _uiState.value.copy(successMessage = null) 
    }
    
    fun clearErrorMessage() { 
        _uiState.value = _uiState.value.copy(errorMessage = null) 
    }

    fun resetForm() {
        _uiState.value = _uiState.value.copy(
            formNama = "",
            formNoHp = "",
            formLokasi = "",
            formUsiaUdang = "",
            selectedGejalaCf = emptyMap(),
            formError = null,
            showHasil = false,
            hasilKuesioner = null,
            hasilResults = emptyList()
        )
    }
}