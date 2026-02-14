package com.example.cfvaname.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Singleton global ticker yang emit "sinyal refresh" setiap 30 detik.
 * 
 * ✅ PERBAIKAN: Sekarang dengan sistem deteksi perubahan data
 * - Menggunakan checksum/hash untuk membandingkan data
 * - Hanya emit signal jika ada perubahan nyata di database
 * - Mencegah refresh UI yang tidak perlu (mengurangi "risih")
 * 
 * Cara pakai di ViewModel:
 * 
 * init {
 *     viewModelScope.launch {
 *         AutoRefreshManager.refreshTick.collect {
 *             loadData() // panggil fungsi fetch kamu
 *         }
 *     }
 * }
 * 
 * Di fungsi loadData(), setelah fetch dari repository:
 * 
 * fun loadData() {
 *     viewModelScope.launch {
 *         val result = repository.getAll()
 *         result.onSuccess { list ->
 *             // Kalkulasi checksum dari data
 *             val checksum = AutoRefreshManager.calculateChecksum(list)
 *             
 *             // Update state hanya jika data berubah
 *             if (AutoRefreshManager.hasChanged("key_unique", checksum)) {
 *                 _uiState.value = _uiState.value.copy(dataList = list)
 *             }
 *         }
 *     }
 * }
 */
object AutoRefreshManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _refreshTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    
    /**
     * Subscribe Flow ini di ViewModel untuk dapat sinyal refresh tiap 30 detik
     */
    val refreshTick: SharedFlow<Unit> = _refreshTick.asSharedFlow()
    
    val intervalMs: Long = 270_000L
    
    /**
     * Map untuk menyimpan checksum terakhir dari setiap data
     * Key: identifier unik (misal: "gejala_list", "hipotesis_list", dll)
     * Value: checksum/hash dari data terakhir
     */
    private val checksumCache = mutableMapOf<String, String>()
    
    init {
        scope.launch {
            while (true) {
                delay(intervalMs)
                _refreshTick.emit(Unit)
            }
        }
    }
    
    /**
     * Paksa semua subscriber refresh sekarang (misal setelah insert/update/delete)
     * PENTING: Gunakan ini setelah operasi write untuk langsung sync
     */
    fun triggerNow() {
        scope.launch {
            _refreshTick.emit(Unit)
        }
    }
    
    /**
     * Kalkulasi checksum dari list data
     * Menggunakan hashCode() kombinasi dari semua item
     * 
     * @param data List data apa saja yang bisa di-hash
     * @return String checksum
     */
    fun <T> calculateChecksum(data: List<T>): String {
        if (data.isEmpty()) return "empty"
        
        // Kombinasi hashCode dari semua item
        // Menggunakan fold untuk akumulasi hash
        val combinedHash = data.fold(0) { acc, item ->
            // XOR untuk kombinasi hash yang lebih baik
            acc xor (item?.hashCode() ?: 0)
        }
        
        return combinedHash.toString()
    }
    
    /**
     * Kalkulasi checksum dari single object
     */
    fun <T> calculateChecksum(data: T): String {
        return data?.hashCode()?.toString() ?: "null"
    }
    
    /**
     * Cek apakah data berubah dengan membandingkan checksum
     * 
     * @param key Identifier unik untuk data (misal: "gejala_list", "dashboard_stats")
     * @param newChecksum Checksum baru dari data
     * @return true jika data berubah, false jika sama
     */
    fun hasChanged(key: String, newChecksum: String): Boolean {
        val oldChecksum = checksumCache[key]
        
        // Jika ini pertama kali atau checksum berbeda
        val changed = oldChecksum == null || oldChecksum != newChecksum
        
        // Update cache dengan checksum baru
        if (changed) {
            checksumCache[key] = newChecksum
        }
        
        return changed
    }
    
    /**
     * Reset checksum cache untuk key tertentu
     * Berguna saat melakukan operasi write (insert/update/delete)
     */
    fun invalidate(key: String) {
        checksumCache.remove(key)
    }
    
    /**
     * Reset semua checksum cache
     * Berguna untuk force refresh semua data
     */
    fun invalidateAll() {
        checksumCache.clear()
    }
    
    /**
     * Helper: Kombinasi invalidate + trigger untuk operasi write
     * Panggil ini setelah insert/update/delete untuk langsung refresh
     * 
     * @param keys Daftar key yang perlu di-invalidate
     */
    fun invalidateAndRefresh(vararg keys: String) {
        keys.forEach { invalidate(it) }
        triggerNow()
    }
}