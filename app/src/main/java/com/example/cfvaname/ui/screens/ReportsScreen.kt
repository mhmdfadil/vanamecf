package com.example.cfvaname.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cfvaname.ui.localization.AppStrings
import com.example.cfvaname.ui.localization.LocalLanguage
import com.example.cfvaname.ui.localization.stringResource
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.viewmodel.PdfFileItem
import com.example.cfvaname.viewmodel.ReportsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// =====================================================
// REPORTS SCREEN
// =====================================================
@Composable
fun ReportsScreen(
    padding: PaddingValues,
    reportsViewModel: ReportsViewModel = viewModel()
) {
    val context = LocalContext.current
    val language = LocalLanguage.current
    val uiState by reportsViewModel.uiState.collectAsState()

    // Preview state
    var previewFile by remember { mutableStateOf<PdfFileItem?>(null) }

    // Load files on first composition
    LaunchedEffect(Unit) {
        reportsViewModel.loadFiles(context)
    }

    // Delete confirmation dialog
    uiState.deleteConfirmFile?.let { file ->
        AlertDialog(
            onDismissRequest = { reportsViewModel.dismissDeleteConfirm() },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = Color(0xFFD32F2F)) },
            title = {
                Text(
                    text = if (language == "en") "Delete File?" else "Hapus File?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "en")
                        "Are you sure you want to delete \"${file.name}.pdf\"? This action cannot be undone."
                    else
                        "Yakin ingin menghapus \"${file.name}.pdf\"? Tindakan ini tidak dapat dikembalikan."
                )
            },
            confirmButton = {
                Button(
                    onClick = { reportsViewModel.deleteFile(context, file) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (uiState.isDeleting)
                            stringResource(AppStrings.Deleting)
                        else
                            stringResource(AppStrings.Delete)
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { reportsViewModel.dismissDeleteConfirm() }) {
                    Text(stringResource(AppStrings.Cancel))
                }
            }
        )
    }

    // PDF Preview dialog
    previewFile?.let { file ->
        PdfPreviewDialog(
            file = file,
            language = language,
            onDismiss = { previewFile = null },
            onShare = { shareFile(context, file) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // =====================================================
        // HEADER
        // =====================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(AppStrings.DiagnosisReports),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )

            // View toggle
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row {
                    IconButton(
                        onClick = { if (uiState.isGridView) reportsViewModel.toggleViewMode() },
                        modifier = Modifier
                            .size(38.dp)
                            .then(
                                if (!uiState.isGridView) Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VenamePrimary)
                                else Modifier
                            )
                    ) {
                        Icon(
                            Icons.Filled.ViewList,
                            contentDescription = "List",
                            tint = if (!uiState.isGridView) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { if (!uiState.isGridView) reportsViewModel.toggleViewMode() },
                        modifier = Modifier
                            .size(38.dp)
                            .then(
                                if (uiState.isGridView) Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VenamePrimary)
                                else Modifier
                            )
                    ) {
                        Icon(
                            Icons.Filled.GridView,
                            contentDescription = "Grid",
                            tint = if (uiState.isGridView) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // =====================================================
        // STORAGE INFO CARD
        // =====================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(VenamePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Storage,
                        contentDescription = null,
                        tint = VenamePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "en") "Storage Used" else "Penyimpanan Digunakan",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = uiState.storageInfo.totalSizeFormatted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VenamePrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${uiState.storageInfo.totalFiles} ${if (language == "en") "files" else "file"}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VenamePrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =====================================================
        // CONTENT
        // =====================================================
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = VenamePrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(AppStrings.Loading),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            uiState.files.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(AppStrings.NoReports),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == "en")
                                "PDF reports from questionnaire diagnosis will appear here"
                            else
                                "Laporan PDF dari diagnosa kuesioner akan muncul di sini",
                            fontSize = 13.sp,
                            color = TextSecondary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }

            else -> {
                if (uiState.isGridView) {
                    // GRID VIEW
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.files) { file ->
                            PdfGridItem(
                                file = file,
                                language = language,
                                onClick = { previewFile = file },
                                onShare = { shareFile(context, file) },
                                onDelete = { reportsViewModel.showDeleteConfirm(file) }
                            )
                        }
                    }
                } else {
                    // LIST VIEW
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.files) { file ->
                            PdfListItem(
                                file = file,
                                language = language,
                                onClick = { previewFile = file },
                                onShare = { shareFile(context, file) },
                                onDelete = { reportsViewModel.showDeleteConfirm(file) }
                            )
                        }
                    }
                }
            }
        }

        // Error
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFDEDED)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// =====================================================
// LIST ITEM
// =====================================================
@Composable
fun PdfListItem(
    file: PdfFileItem,
    language: String,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFD32F2F).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${file.name}.pdf",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.sizeFormatted,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "  •  ",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = file.lastModifiedFormatted,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Actions
            IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = if (language == "en") "Share" else "Bagikan",
                    tint = VenamePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = stringResource(AppStrings.Delete),
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =====================================================
// GRID ITEM
// =====================================================
@Composable
fun PdfGridItem(
    file: PdfFileItem,
    language: String,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    // Load thumbnail
    val context = LocalContext.current
    var thumbnail by remember(file.file.absolutePath) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(file.file.absolutePath) {
        thumbnail = withContext(Dispatchers.IO) {
            renderPdfThumbnail(file.file, width = 300)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column {
            // Thumbnail or placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F).copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PDF",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "${file.name}.pdf",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${file.sizeFormatted} • ${file.lastModifiedFormatted}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row {
                    IconButton(onClick = onShare, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = null,
                            tint = VenamePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// PDF PREVIEW DIALOG (full-screen, scrollable pages)
// =====================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewDialog(
    file: PdfFileItem,
    language: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalPages by remember { mutableIntStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(file.file.absolutePath) {
        isLoading = true
        try {
            val result = withContext(Dispatchers.IO) {
                renderAllPdfPages(file.file, maxWidth = 1200)
            }
            pages = result
            totalPages = result.size
        } catch (e: Exception) {
            errorMsg = e.localizedMessage
        }
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "${file.name}.pdf",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (totalPages > 0) {
                                Text(
                                    text = "$totalPages ${if (language == "en") "pages" else "halaman"} • ${file.sizeFormatted}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(AppStrings.Close))
                        }
                    },
                    actions = {
                        IconButton(onClick = onShare) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = if (language == "en") "Share" else "Bagikan",
                                tint = VenamePrimary
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VenamePrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (language == "en") "Rendering PDF..." else "Memuat PDF...",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                errorMsg != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (language == "en") "Failed to load PDF"
                                else "Gagal memuat PDF",
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMsg ?: "",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFFE0E0E0)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(pages.size) { index ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Page label
                                Text(
                                    text = "${if (language == "en") "Page" else "Hal."} ${index + 1} / $totalPages",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                // Page image
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    shadowElevation = 3.dp,
                                    color = Color.White
                                ) {
                                    Image(
                                        bitmap = pages[index].asImageBitmap(),
                                        contentDescription = "Page ${index + 1}",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// UTILITY FUNCTIONS
// =====================================================

/**
 * Render first page of PDF as thumbnail
 */
private fun renderPdfThumbnail(file: File, width: Int = 300): Bitmap? {
    return try {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        if (renderer.pageCount == 0) {
            renderer.close()
            fd.close()
            return null
        }
        val page = renderer.openPage(0)
        val scale = width.toFloat() / page.width
        val height = (page.height * scale).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        fd.close()
        bitmap
    } catch (_: Exception) {
        null
    }
}

/**
 * Render all pages of PDF
 */
private fun renderAllPdfPages(file: File, maxWidth: Int = 1200): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(fd)

    for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)
        val scale = maxWidth.toFloat() / page.width
        val width = maxWidth
        val height = (page.height * scale).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmaps.add(bitmap)
    }

    renderer.close()
    fd.close()
    return bitmaps
}

/**
 * Share PDF file via intent
 */
private fun shareFile(context: Context, file: PdfFileItem) {
    try {
        val uri = file.uri ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Share PDF")
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}