package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.AutoRefreshManager
import com.example.cfvaname.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalGejala: Int = 0,
    val totalHipotesis: Int = 0,
    val totalKuesioner: Int = 0,
    val totalNilaiCf: Int = 0
)

data class HipotesisWithGejalaCount(
    val hipotesis: Hipotesis,
    val gejalaCount: Int
)

data class KuesionerWithDetail(
    val kuesioner: Kuesioner,
    val gejalaCount: Int
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val stats: DashboardStats = DashboardStats(),
    val topHipotesis: List<HipotesisWithGejalaCount> = emptyList(),
    val recentKuesioner: List<KuesionerWithDetail> = emptyList(),
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {

    private val gejalaRepository = GejalaRepository()
    private val hipotesisRepository = HipotesisRepository()
    private val kuesionerRepository = KuesionerRepository()
    private val ghRepository = GejalaHipotesisRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        // ✅ Auto-refresh global
        viewModelScope.launch {
            AutoRefreshManager.refreshTick.collect { loadDashboardData() }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Fetch all data
                val gejalaResult = gejalaRepository.getAll()
                val hipotesisResult = hipotesisRepository.getAll()
                val kuesionerResult = kuesionerRepository.getAllKuesioner()
                val nilaiCfResult = kuesionerRepository.getAllNilaiCf()
                val ghResult = ghRepository.getAll()

                val gejalas = gejalaResult.getOrDefault(emptyList())
                val hipotesisList = hipotesisResult.getOrDefault(emptyList())
                val kuesioners = kuesionerResult.getOrDefault(emptyList())
                val nilaiCfs = nilaiCfResult.getOrDefault(emptyList())
                val ghList = ghResult.getOrDefault(emptyList())

                // Stats
                val stats = DashboardStats(
                    totalGejala = gejalas.size,
                    totalHipotesis = hipotesisList.size,
                    totalKuesioner = kuesioners.size,
                    totalNilaiCf = nilaiCfs.size
                )

                // Top hipotesis by gejala count (via gejala_hipotesis pivot)
                val topHipotesis = hipotesisList.map { h ->
                    val count = ghList.count { it.hipotesisId == h.id }
                    HipotesisWithGejalaCount(hipotesis = h, gejalaCount = count)
                }
                    .sortedByDescending { it.gejalaCount }
                    .take(5)

                // Recent kuesioner (already sorted desc by created_at from repo)
                val recentKuesioner = kuesioners.take(5).map { k ->
                    val count = try {
                        kuesionerRepository.countKuesionerData(k.id)
                    } catch (_: Exception) { 0 }
                    KuesionerWithDetail(kuesioner = k, gejalaCount = count)
                }

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    stats = stats,
                    topHipotesis = topHipotesis,
                    recentKuesioner = recentKuesioner
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat dashboard: ${e.localizedMessage}"
                )
            }
        }
    }
}