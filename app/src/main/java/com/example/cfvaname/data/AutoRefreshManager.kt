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
 * Cara pakai di ViewModel manapun:
 *
 *   init {
 *       viewModelScope.launch {
 *           AutoRefreshManager.refreshTick.collect {
 *               loadData() // panggil fungsi fetch kamu
 *           }
 *       }
 *   }
 *
 * Cukup tambahkan 3-4 baris itu di setiap ViewModel yang butuh auto-refresh.
 * Tidak perlu ubah Repository sama sekali.
 */
object AutoRefreshManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _refreshTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Subscribe Flow ini di ViewModel untuk dapat sinyal refresh tiap 30 detik */
    val refreshTick: SharedFlow<Unit> = _refreshTick.asSharedFlow()

    val intervalMs: Long = 30_000L

    init {
        scope.launch {
            while (true) {
                delay(intervalMs)
                _refreshTick.emit(Unit)
            }
        }
    }

    /** Paksa semua subscriber refresh sekarang (misal setelah insert/update/delete) */
    fun triggerNow() {
        scope.launch {
            _refreshTick.emit(Unit)
        }
    }
}