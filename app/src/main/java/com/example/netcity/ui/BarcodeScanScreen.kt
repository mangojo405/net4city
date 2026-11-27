package com.example.netcity.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.netcity.data.QrCodeGenerator
import com.example.netcity.model.BarcodeObject
import com.example.netcity.model.WorkspacePersona
import com.example.netcity.viewmodel.NetCitySnapshot
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.netcity.viewmodel.ScanResultUi
import com.example.ui.theme.*

@Composable
fun BarcodeScanScreen(
    snapshot: NetCitySnapshot,
    lastScanResult: ScanResultUi?,
    viewModel: NetCityViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var scannerBuffer by remember { mutableStateOf("") }
    var activeStudentName by remember(snapshot.classStudents) {
        mutableStateOf(snapshot.classStudents.firstOrNull()?.fullName ?: "Raka Pratama")
    }
    var searchQuery by remember { mutableStateOf("") }
    var previewQrBarcode by remember { mutableStateOf<BarcodeObject?>(null) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    val canEditRegistry = snapshot.activePersona == WorkspacePersona.ADMIN || snapshot.activePersona == WorkspacePersona.GURU

    fun triggerScan(raw: String) {
        if (raw.isBlank()) return
        viewModel.processScannedBarcode(
            rawInput = raw,
            snapshot = snapshot,
            studentName = activeStudentName,
            sessionName = snapshot.activeClass?.activeSessionMission ?: "Sesi PID"
        )
        scannerBuffer = ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("SCAN MODE PID") },
                icon = { Icon(Icons.Rounded.QrCodeScanner, contentDescription = null) },
                modifier = Modifier.testTag("tab_scan_mode")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Registry (${snapshot.barcodes.size})") },
                icon = { Icon(Icons.Rounded.Inventory2, contentDescription = null) },
                modifier = Modifier.testTag("tab_barcode_registry")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Riwayat (${snapshot.scanLogs.size})") },
                icon = { Icon(Icons.Rounded.History, contentDescription = null) },
                modifier = Modifier.testTag("tab_scan_history")
            )
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Honest Scope Notice (Per Specification Section 8)
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text(
                                    text = "USB Barcode/QR Scanner bekerja sebagai keyboard wedge (diakhiri Enter). Scanner hanya mengidentifikasi ID objek fisik; posisi modul diorama & sambungan kabel fisik diverifikasi manual oleh guru.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Active PID Scanner Wedge Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = NetNavyDeep)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "MODE SCANNER USB (PID READY)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = NetCyanBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Kelas Aktif: ${snapshot.activeClass?.code ?: "X TKJ 1"} • Identitas permanen lintas kelas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                    Icon(Icons.Rounded.Usb, contentDescription = null, tint = NetEmeraldBright)
                                }

                                OutlinedTextField(
                                    value = activeStudentName,
                                    onValueChange = { activeStudentName = it },
                                    label = { Text("Siswa Pengguna Sesi", color = Color(0xFFCBD5E1)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NetCyanBright,
                                        unfocusedBorderColor = Color(0xFF475569)
                                    )
                                )

                                OutlinedTextField(
                                    value = scannerBuffer,
                                    onValueChange = { scannerBuffer = it },
                                    label = { Text("Arahkan USB Scanner lalu tekan Enter (mis. NC-RTR-001)", color = Color(0xFFCBD5E1)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { triggerScan(scannerBuffer) }),
                                    trailingIcon = {
                                        IconButton(onClick = { triggerScan(scannerBuffer) }) {
                                            Icon(Icons.Rounded.Search, contentDescription = "Cari Kode", tint = NetCyanBright)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("usb_scanner_input")
                                        .onPreviewKeyEvent { event ->
                                            if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                                                triggerScan(scannerBuffer)
                                                true
                                            } else false
                                        },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NetEmeraldBright,
                                        unfocusedBorderColor = NetCyanBright
                                    )
                                )

                                Text(
                                    text = "Uji Cepat Scan Objek Permanen:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("NC-RTR-001", "NC-SWT-001", "NC-PC-001", "NC-SRV-001", "NC-ODC-001", "NC-CHAR-00003").forEach { sample ->
                                        AssistChip(
                                            onClick = { triggerScan(sample) },
                                            label = { Text(sample, color = Color.White) },
                                            modifier = Modifier.testTag("quick_scan_$sample")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Last Scan Result Detail Card
                    if (lastScanResult != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (lastScanResult.foundObject != null || lastScanResult.foundCharacter != null) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                            contentDescription = null,
                                            tint = if (lastScanResult.foundObject != null || lastScanResult.foundCharacter != null) NetEmerald else NetCoralError
                                        )
                                        Text(
                                            text = "Hasil Pindai: ${lastScanResult.code}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = lastScanResult.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (lastScanResult.addedToInventory) NetEmerald else NetAmber
                                    )

                                    lastScanResult.foundObject?.let { obj ->
                                        HorizontalDivider()
                                        Text(obj.objectName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("Kategori: ${obj.category} • Level ${obj.relatedLevel} • Lokasi: ${obj.storageLocation}", style = MaterialTheme.typography.labelMedium, color = NetCyanPrimary)
                                        Text("Fungsi & Materi: ${obj.functionDesc}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Spesifikasi: ${obj.specs}", style = MaterialTheme.typography.bodySmall)
                                        Text("Port: ${obj.portCount} • Harga Virtual: NC$${obj.virtualPrice} • Kebutuhan: ${obj.installNeeds}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    lastScanResult.foundCharacter?.let { ch ->
                                        HorizontalDivider()
                                        Text("Karakter Terdeteksi: ${ch.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("Pemilik: ${ch.ownerName} • Profesi: ${ch.profession} • Lv.${ch.level} (${ch.xp} XP)", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                val filtered = remember(snapshot.barcodes, searchQuery) {
                    if (searchQuery.isBlank()) snapshot.barcodes
                    else snapshot.barcodes.filter {
                        it.barcodeCode.contains(searchQuery, ignoreCase = true) ||
                            it.objectName.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari kode/nama perangkat...") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                            if (canEditRegistry) {
                                FilledTonalButton(
                                    onClick = { showRegisterDialog = true },
                                    modifier = Modifier.testTag("register_barcode_button")
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Daftar")
                                }
                            }
                        }
                    }

                    items(filtered, key = { it.id }) { obj ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(obj.barcodeCode, style = MaterialTheme.typography.labelMedium, color = NetCyanPrimary, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (obj.inventoryStatus == "TERSEDIA") NetEmeraldContainer else NetAmberContainer
                                        ) {
                                            Text(
                                                text = obj.inventoryStatus,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (obj.inventoryStatus == "TERSEDIA") NetOnEmeraldContainer else NetOnAmberContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(obj.objectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${obj.category} • Rak: ${obj.storageLocation} • Lv.${obj.relatedLevel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (obj.activeClassId.isNotBlank()) {
                                        Text("Terakhir dipakai: ${obj.activeClassId.uppercase().replace("_", " ")} (${obj.activeStudentName})", style = MaterialTheme.typography.labelSmall, color = NetAmber)
                                    }
                                }
                                IconButton(onClick = { previewQrBarcode = obj }) {
                                    Icon(Icons.Rounded.QrCode2, contentDescription = "Lihat QR ${obj.barcodeCode}", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (snapshot.scanLogs.isEmpty()) {
                        item {
                            Text("Belum ada riwayat pemindaian.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    items(snapshot.scanLogs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${log.barcodeCode} — ${log.objectName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (log.addedToInventory) "+Inventory" else "Teridentifikasi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (log.addedToInventory) NetEmerald else NetCyanPrimary
                                    )
                                }
                                Text("${log.classId.uppercase().replace("_", " ")} • ${log.studentName} • ${log.sessionName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(log.scanNote, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // QR Code Preview Modal for Printing
    if (previewQrBarcode != null) {
        val obj = previewQrBarcode!!
        val bmp = remember(obj.barcodeCode) { QrCodeGenerator.generateQrBitmap(obj.barcodeCode, 340) }
        AlertDialog(
            onDismissRequest = { previewQrBarcode = null },
            title = { Text("Label QR Permanen: ${obj.barcodeCode}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "QR ${obj.barcodeCode}",
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        )
                    }
                    Text(obj.objectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Lokasi Simpan: ${obj.storageLocation}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Tempelkan label permanen ini pada objek fisik diorama. Tidak perlu membuat barcode baru saat dipakai kelas berbeda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { previewQrBarcode = null }) { Text("Tutup") }
            }
        )
    }

    // Register New Barcode Object Dialog
    if (showRegisterDialog) {
        var code by remember { mutableStateOf("NC-RTR-003") }
        var objName by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Router") }
        var functionDesc by remember { mutableStateOf("") }
        var specs by remember { mutableStateOf("") }
        var storage by remember { mutableStateOf("Rak Lab B2") }
        var level by remember { mutableStateOf("7") }

        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Registrasi Objek Barcode Permanen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Kode ID Permanen (mis. NC-RTR-003)") }, singleLine = true)
                    OutlinedTextField(value = objName, onValueChange = { objName = it }, label = { Text("Nama Perangkat / Bangunan") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, singleLine = true)
                    OutlinedTextField(value = functionDesc, onValueChange = { functionDesc = it }, label = { Text("Fungsi Teknis") })
                    OutlinedTextField(value = specs, onValueChange = { specs = it }, label = { Text("Spesifikasi") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = storage, onValueChange = { storage = it }, label = { Text("Lokasi Rak") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text("Level (1-10)") }, modifier = Modifier.width(100.dp), singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveBarcodeObject(
                        existingId = null,
                        barcodeCode = code,
                        objectName = objName,
                        category = category,
                        functionDesc = functionDesc,
                        specs = specs,
                        portCount = 4,
                        virtualPrice = 300,
                        installNeeds = "Kabel power & UTP",
                        relatedLevel = level.toIntOrNull() ?: 1,
                        storageLocation = storage,
                        inventoryStatus = "TERSEDIA"
                    )
                    showRegisterDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) { Text("Batal") }
            }
        )
    }
}
