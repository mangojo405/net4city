package com.example.netcity.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.netcity.model.*
import com.example.netcity.viewmodel.NetCitySnapshot
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.ui.theme.NetAmber
import com.example.ui.theme.NetCyanPrimary
import com.example.ui.theme.NetEmerald

@Composable
fun ManagementScreen(
    snapshot: NetCitySnapshot,
    viewModel: NetCityViewModel
) {
    val persona = snapshot.activePersona
    val canManageAll = persona == WorkspacePersona.ADMIN
    val canManageClass = persona == WorkspacePersona.ADMIN || persona == WorkspacePersona.GURU

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddClassDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassRoom?>(null) }
    var showAddMemberDialog by remember { mutableStateOf<String?>(null) } // "GURU" or "SISWA"
    var showCsvImportDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Kelas (${snapshot.classes.size})") },
                icon = { Icon(Icons.Rounded.MeetingRoom, contentDescription = null) },
                modifier = Modifier.testTag("tab_classes")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Siswa (${snapshot.students.size})") },
                icon = { Icon(Icons.Rounded.Groups, contentDescription = null) },
                modifier = Modifier.testTag("tab_students")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Guru (${snapshot.teachers.size})") },
                icon = { Icon(Icons.Rounded.School, contentDescription = null) },
                modifier = Modifier.testTag("tab_teachers")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Admin (${snapshot.admins.size.coerceAtLeast(1)})") },
                icon = { Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null) },
                modifier = Modifier.testTag("tab_admins")
            )
        }

        if (!canManageClass) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Mode Siswa (Hanya Baca): Siswa dapat melihat daftar kelas & kelompok praktikum, namun tidak dapat mengubah struktur kelas atau akun.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (canManageAll) {
                        item {
                            Button(
                                onClick = {
                                    editingClass = null
                                    showAddClassDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_class_button")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Tambah Kelas Baru")
                            }
                        }
                    }

                    items(snapshot.classes, key = { it.id }) { cls ->
                        val countSiswa = snapshot.students.count { it.classId == cls.id }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = cls.code,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = NetCyanPrimary
                                            )
                                            if (cls.demoData) {
                                                AssistChip(
                                                    onClick = {},
                                                    label = { Text("Simulasi", style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Guru Pengampu: ${cls.teacherName}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "$countSiswa Siswa • Kelompok: ${cls.labGroups.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Teacher / Admin Level Lock-Unlock Slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Level Terbuka: Lv.${cls.maxUnlockedLevel} / 10",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = NetEmerald
                                    )
                                    if (canManageClass) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedIconButton(
                                                onClick = {
                                                    val next = (cls.maxUnlockedLevel - 1).coerceAtLeast(1).toInt()
                                                    viewModel.saveClassRoom(cls.id, cls.code, cls.name, cls.teacherName, next, cls.activeSessionMission, cls.labGroups)
                                                }
                                            ) {
                                                Icon(Icons.Rounded.Lock, contentDescription = "Kunci 1 level")
                                            }
                                            FilledTonalIconButton(
                                                onClick = {
                                                    val next = (cls.maxUnlockedLevel + 1).coerceAtMost(10).toInt()
                                                    viewModel.saveClassRoom(cls.id, cls.code, cls.name, cls.teacherName, next, cls.activeSessionMission, cls.labGroups)
                                                }
                                            ) {
                                                Icon(Icons.Rounded.LockOpen, contentDescription = "Buka 1 level")
                                            }
                                            IconButton(
                                                onClick = {
                                                    editingClass = cls
                                                    showAddClassDialog = true
                                                }
                                            ) {
                                                Icon(Icons.Rounded.Edit, contentDescription = "Edit kelas")
                                            }
                                            if (canManageAll) {
                                                IconButton(onClick = { viewModel.deleteClassRoom(cls.id) }) {
                                                    Icon(Icons.Rounded.Delete, contentDescription = "Hapus kelas", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                var filterClass by remember { mutableStateOf("ALL") }
                val filteredStudents = remember(snapshot.students, filterClass) {
                    if (filterClass == "ALL") snapshot.students
                    else snapshot.students.filter { it.classId == filterClass }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canManageAll) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showAddMemberDialog = "SISWA" },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_student_button")
                                ) {
                                    Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Tambah Siswa")
                                }
                                FilledTonalButton(
                                    onClick = { showCsvImportDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("import_csv_button")
                                ) {
                                    Icon(Icons.Rounded.UploadFile, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Impor CSV")
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = filterClass == "ALL",
                                onClick = { filterClass = "ALL" },
                                label = { Text("Semua") }
                            )
                            snapshot.classes.take(4).forEach { cls ->
                                FilterChip(
                                    selected = filterClass == cls.id,
                                    onClick = { filterClass = cls.id },
                                    label = { Text(cls.code) }
                                )
                            }
                        }
                    }

                    items(filteredStudents, key = { it.id }) { student ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${student.identityNumber} • ${student.classId.uppercase().replace("_", " ")} • ${student.groupName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Lv.${student.level} • ${student.xp} XP" + if (student.characterId.isNotBlank()) " • QR: ${student.characterId}" else "",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NetAmber
                                    )
                                }
                                if (canManageAll) {
                                    IconButton(onClick = { viewModel.deleteMember(student.id) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus siswa", tint = MaterialTheme.colorScheme.error)
                                    }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canManageAll) {
                        item {
                            Button(
                                onClick = { showAddMemberDialog = "GURU" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_teacher_button")
                            ) {
                                Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Tambah Guru Pengampu")
                            }
                        }
                    }
                    items(snapshot.teachers, key = { it.id }) { teacher ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(teacher.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${teacher.identityNumber} • ${teacher.groupName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Kelas Ampuan: ${teacher.assignedClasses.joinToString(", ") { it.uppercase().replace("_", " ") }}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NetCyanPrimary
                                    )
                                }
                                if (canManageAll) {
                                    IconButton(onClick = { viewModel.deleteMember(teacher.id) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus guru", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(
                                        text = "Database Administrator Utama (Firestore /admins)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "Nama: Dabojo\nEmail: dabojo.1487@gmail.com\nHak Akses: Bootstrapped Verified Admin (Akses Penuh & Override Penilaian)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Button(
                                    onClick = { viewModel.syncPrimaryAdmin("Dabojo", "dabojo.1487@gmail.com") },
                                    modifier = Modifier.testTag("sync_dabojo_admin_button")
                                ) {
                                    Icon(Icons.Rounded.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Sinkronkan Dabojo ke Database Admin")
                                }
                            }
                        }
                    }

                    items(snapshot.admins, key = { it.id }) { admin ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(admin.displayName.ifBlank { "Dabojo" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(admin.email, style = MaterialTheme.typography.bodySmall, color = NetCyanPrimary)
                                    Text("UID: ${admin.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Rounded.Verified, contentDescription = "Admin Terverifikasi", tint = NetEmerald)
                            }
                        }
                    }
                }
            }
        }
    }

    // Class Create/Edit Dialog
    if (showAddClassDialog) {
        var code by remember { mutableStateOf(editingClass?.code ?: "X TKJ 5") }
        var name by remember { mutableStateOf(editingClass?.name ?: "Teknik Jaringan Komputer 5") }
        var teacher by remember { mutableStateOf(editingClass?.teacherName ?: "Pak Hendra Wijaya, S.Kom") }
        var mission by remember { mutableStateOf(editingClass?.activeSessionMission ?: "Level 1 - Computer Town") }
        var groupsText by remember { mutableStateOf(editingClass?.labGroups?.joinToString(", ") ?: "Kelompok Alpha, Kelompok Beta") }
        var maxLv by remember { mutableIntStateOf(editingClass?.maxUnlockedLevel?.toInt() ?: 1) }

        AlertDialog(
            onDismissRequest = { showAddClassDialog = false },
            title = { Text(if (editingClass == null) "Tambah Kelas TKJ" else "Edit Kelas ${editingClass?.code}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Kode Kelas") }, singleLine = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Kelas") }, singleLine = true)
                    OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Guru Pengampu") }, singleLine = true)
                    OutlinedTextField(value = mission, onValueChange = { mission = it }, label = { Text("Sesi Praktik Aktif") }, singleLine = true)
                    OutlinedTextField(value = groupsText, onValueChange = { groupsText = it }, label = { Text("Kelompok (pisahkan koma)") }, singleLine = true)
                    Text("Level Terbuka: $maxLv / 10", style = MaterialTheme.typography.labelLarge)
                    Slider(value = maxLv.toFloat(), onValueChange = { maxLv = it.toInt() }, valueRange = 1f..10f, steps = 8)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val groups = groupsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    viewModel.saveClassRoom(editingClass?.id, code, name, teacher, maxLv, mission, groups)
                    showAddClassDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddClassDialog = false }) { Text("Batal") }
            }
        )
    }

    // Add Member Dialog (Guru / Siswa)
    if (showAddMemberDialog != null) {
        val type = showAddMemberDialog!!
        var fullName by remember { mutableStateOf("") }
        var idNumber by remember { mutableStateOf(if (type == "GURU") "NIP-2026-003" else "NIS-2026-021") }
        var classId by remember { mutableStateOf(snapshot.selectedClassId) }
        var groupName by remember { mutableStateOf(if (type == "GURU") "Pengampu TJKT" else "Kelompok Alpha") }

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = null },
            title = { Text("Tambah Data $type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, singleLine = true)
                    OutlinedTextField(value = idNumber, onValueChange = { idNumber = it }, label = { Text(if (type == "GURU") "NIP" else "NIS") }, singleLine = true)
                    OutlinedTextField(value = classId, onValueChange = { classId = it }, label = { Text("ID Kelas (mis. x_tkj_1)") }, singleLine = true)
                    OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text(if (type == "GURU") "Bidang / Jabatan" else "Kelompok Praktik") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveMember(
                        existingId = null,
                        memberType = type,
                        fullName = fullName,
                        identityNumber = idNumber,
                        classId = classId,
                        assignedClasses = if (type == "GURU") listOf(classId) else emptyList(),
                        groupName = groupName
                    )
                    showAddMemberDialog = null
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = null }) { Text("Batal") }
            }
        )
    }

    // Bulk CSV Import Dialog for Students
    if (showCsvImportDialog) {
        var csvInput by remember {
            mutableStateOf(
                "Nama,NIS,Kelas,Kelompok\nArif Budiman,NIS-2026-101,x_tkj_1,Kelompok Alpha\nSalsa Nabila,NIS-2026-102,x_tkj_2,Kelompok Beta"
            )
        }
        AlertDialog(
            onDismissRequest = { showCsvImportDialog = false },
            title = { Text("Impor Data Siswa dari CSV") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Format per baris: Nama,NIS,Kelas,Kelompok (data langsung disimpan ke Cloud Firestore).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = csvInput,
                        onValueChange = { csvInput = it },
                        minLines = 5,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.importStudentsCsv(csvInput, snapshot.selectedClassId)
                    showCsvImportDialog = false
                }) { Text("Impor Sekarang") }
            },
            dismissButton = {
                TextButton(onClick = { showCsvImportDialog = false }) { Text("Batal") }
            }
        )
    }
}
