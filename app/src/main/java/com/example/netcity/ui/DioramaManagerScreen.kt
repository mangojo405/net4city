package com.example.netcity.ui

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.netcity.data.DioramaPapercraftPrinter
import com.example.netcity.model.DioramaModule
import com.example.netcity.model.WorkspacePersona
import com.example.netcity.viewmodel.NetCitySnapshot
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DioramaManagerScreen(
    snapshot: NetCitySnapshot,
    viewModel: NetCityViewModel
) {
    val context = LocalContext.current
    var selectedLevelFilter by remember { mutableIntStateOf(0) } // 0 = Semua Level
    var previewModule by remember { mutableStateOf<DioramaModule?>(null) }
    val canVerify = snapshot.activePersona == WorkspacePersona.ADMIN || snapshot.activePersona == WorkspacePersona.GURU

    val modules = remember(snapshot.dioramaModules, selectedLevelFilter) {
        if (selectedLevelFilter == 0) snapshot.dioramaModules
        else snapshot.dioramaModules.filter { it.requiredLevels.contains(selectedLevelFilter.toLong()) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Diorama Modular Bersama (Siap Cetak Kertas 3D)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Satu set modul diorama fisik dipakai bergantian antar kelas dengan identitas barcode permanen. Cetak pola kertas 3D lengkap dengan QR & Barcode 1D untuk digunting, dilipat, dan dirakit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Button(
                        onClick = {
                            if (modules.isNotEmpty()) {
                                val label = if (selectedLevelFilter == 0) "NET4CITY_Semua_Diorama_3D"
                                else "NET4CITY_Diorama_Level_$selectedLevelFilter"
                                DioramaPapercraftPrinter.printDioramaModules(
                                    context = context,
                                    modules = modules,
                                    jobTitle = label
                                )
                            }
                        },
                        enabled = modules.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("print_all_diorama_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selectedLevelFilter == 0)
                                "Print Semua Pola Diorama + Barcode (${modules.size} Hal. A4)"
                            else
                                "Print Pola Diorama Lv.$selectedLevelFilter (${modules.size} Hal. A4)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Level Filter Bar (1 - 10)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Kelompokkan Modul Berdasarkan Level:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedLevelFilter == 0,
                        onClick = { selectedLevelFilter = 0 },
                        label = { Text("Semua (${snapshot.dioramaModules.size})") }
                    )
                    (1..10).forEach { lv ->
                        FilterChip(
                            selected = selectedLevelFilter == lv,
                            onClick = { selectedLevelFilter = lv },
                            label = { Text("Lv.$lv") },
                            modifier = Modifier.testTag("diorama_level_filter_$lv")
                        )
                    }
                }
            }
        }

        items(modules, key = { it.id }) { mod ->
            val inUse = mod.moduleStatus == "DIGUNAKAN"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(mod.moduleCode, style = MaterialTheme.typography.labelMedium, color = NetCyanPrimary, fontWeight = FontWeight.Bold)
                                Text("• QR: ${mod.barcodeCode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(mod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (inUse) NetAmberContainer else NetEmeraldContainer
                        ) {
                            Text(
                                text = mod.moduleStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (inUse) NetOnAmberContainer else NetOnEmeraldContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(mod.description, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Lokasi: ${mod.storageLocation} • Dipakai di Level: ${mod.requiredLevels.joinToString(", ") { "Lv.$it" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (inUse && mod.activeClassId.isNotBlank()) {
                        Text(
                            text = "Sedang digunakan oleh ${mod.activeClassId.uppercase().replace("_", " ")} (${mod.activeSessionLabel})",
                            style = MaterialTheme.typography.labelMedium,
                            color = NetAmber
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (mod.verifiedByTeacher) Icons.Rounded.Verified else Icons.Rounded.PendingActions,
                                contentDescription = null,
                                tint = if (mod.verifiedByTeacher) NetEmerald else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (mod.verifiedByTeacher) "Terverifikasi Guru" else "Belum Diverifikasi",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (mod.verifiedByTeacher) NetEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (canVerify) {
                                OutlinedButton(
                                    onClick = { viewModel.setDioramaTeacherVerification(mod, !mod.verifiedByTeacher) },
                                    modifier = Modifier.testTag("verify_diorama_${mod.moduleCode}")
                                ) {
                                    Text(if (mod.verifiedByTeacher) "Batal Verif" else "Verifikasi")
                                }
                            }
                            FilledTonalButton(
                                onClick = { viewModel.toggleDioramaModule(mod, snapshot) },
                                modifier = Modifier.testTag("toggle_diorama_${mod.moduleCode}")
                            ) {
                                Text(if (inUse) "Kembalikan" else "Pakai Sesi")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    OutlinedButton(
                        onClick = { previewModule = mod },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("print_diorama_${mod.moduleCode}"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, NetCyanPrimary.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = NetCyanContainer.copy(alpha = 0.3f),
                            contentColor = NetOnCyanContainer
                        )
                    ) {
                        Icon(Icons.Rounded.ContentCut, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Print Pola Kertas 3D + Barcode (${mod.barcodeCode})",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    val activePreview = previewModule
    if (activePreview != null) {
        DioramaPapercraftPreviewDialog(
            module = activePreview,
            onDismiss = { previewModule = null },
            onPrint = {
                DioramaPapercraftPrinter.printDioramaModules(
                    context = context,
                    modules = listOf(activePreview),
                    jobTitle = "NET4CITY_${activePreview.moduleCode}_${activePreview.barcodeCode}"
                )
            }
        )
    }
}

@Composable
private fun DioramaPapercraftPreviewDialog(
    module: DioramaModule,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    var previewBitmap by remember(module.id) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(module.id) {
        previewBitmap = withContext(Dispatchers.Default) {
            DioramaPapercraftPrinter.renderPreviewBitmap(module, scale = 1.5f)
        }
    }

    DisposableEffect(previewBitmap) {
        onDispose {
            // Let Compose release image safely; bitmap GC'd once dialog exits
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pratinjau Pola Cetak Diorama 3D",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${module.moduleCode} • ${module.name} (${module.barcodeCode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_diorama_preview_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Tutup pratinjau")
                    }
                }

                // Helpful Papercraft Specs Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Kertas A4") },
                        leadingIcon = { Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("QR + Barcode 1D") },
                        leadingIcon = { Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("7 Tab Lem & Garis Lipat") },
                        leadingIcon = { Icon(Icons.Rounded.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                // Live Rendered A4 Sheet Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE2E8F0))
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val bmp = previewBitmap
                    if (bmp == null) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Menyiapkan pola jaring-jaring 3D & barcode...")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 6.dp,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Pratinjau pola diorama kertas 3D ${module.name}",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Tutup")
                    }
                    Button(
                        onClick = onPrint,
                        modifier = Modifier
                            .weight(1.6f)
                            .heightIn(min = 48.dp)
                            .testTag("confirm_print_diorama_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cetak / Simpan PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
