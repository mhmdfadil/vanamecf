package com.example.cfvaname.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class PdfFileItem(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val lastModified: Long,
    val lastModifiedFormatted: String,
    val uri: Uri? = null
)

data class StorageInfo(
    val totalFiles: Int = 0,
    val totalSizeBytes: Long = 0,
    val totalSizeFormatted: String = "0 B"
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val files: List<PdfFileItem> = emptyList(),
    val storageInfo: StorageInfo = StorageInfo(),
    val isGridView: Boolean = false,
    val errorMessage: String? = null,
    val deleteConfirmFile: PdfFileItem? = null,
    val isDeleting: Boolean = false
)

class ReportsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun loadFiles(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val files = withContext(Dispatchers.IO) {
                    // PERBAIKAN: Gunakan getExternalFilesDir seperti di KuesionerScreen
                    val reportsDir = File(context.getExternalFilesDir(null), "VENAME_Reports")
                    if (!reportsDir.exists()) reportsDir.mkdirs()

                    val pdfFiles = reportsDir.listFiles { f -> f.extension.equals("pdf", true) }
                        ?.sortedByDescending { it.lastModified() }
                        ?: emptyList()

                    pdfFiles.map { file ->
                        val uri = try {
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                        } catch (_: Exception) { null }

                        PdfFileItem(
                            file = file,
                            name = file.nameWithoutExtension,
                            sizeBytes = file.length(),
                            sizeFormatted = formatFileSize(file.length()),
                            lastModified = file.lastModified(),
                            lastModifiedFormatted = formatDate(file.lastModified()),
                            uri = uri
                        )
                    }
                }

                val totalSize = files.sumOf { it.sizeBytes }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    files = files,
                    storageInfo = StorageInfo(
                        totalFiles = files.size,
                        totalSizeBytes = totalSize,
                        totalSizeFormatted = formatFileSize(totalSize)
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat file: ${e.localizedMessage}"
                )
            }
        }
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isGridView = !_uiState.value.isGridView)
    }

    fun showDeleteConfirm(file: PdfFileItem) {
        _uiState.value = _uiState.value.copy(deleteConfirmFile = file)
    }

    fun dismissDeleteConfirm() {
        _uiState.value = _uiState.value.copy(deleteConfirmFile = null)
    }

    fun deleteFile(context: Context, file: PdfFileItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            try {
                withContext(Dispatchers.IO) {
                    file.file.delete()
                }
                _uiState.value = _uiState.value.copy(isDeleting = false, deleteConfirmFile = null)
                loadFiles(context)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Gagal menghapus: ${e.localizedMessage}"
                )
            }
        }
    }

    companion object {
        fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
                else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            }
        }

        fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}