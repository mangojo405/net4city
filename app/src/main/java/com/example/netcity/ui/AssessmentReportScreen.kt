package com.example.netcity.ui

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.netcity.model.AssessmentRecord
import com.example.netcity.model.CurriculumCatalog
import com.example.netcity.model.WorkspacePersona
import com.example.netcity.viewmodel.NetCitySnapshot
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.ui.theme.*

@Composable
fun AssessmentReportScreen(
    snapshot: NetCitySnapshot,
    viewModel: NetCityViewModel
) {
    val context = LocalContext.current
    val canGrade = snapshot.activePersona == WorkspacePersona.ADMIN || snapshot.activePersona == WorkspacePersona.GURU

    var selectedTab by remember { mutableIntStateOf(0) }
    var reportScope by remember { mutableStateOf("KESELURUHAN") } // INDIVIDU, KELOMPOK, KELAS, KESELURUHAN
    var showGradeDialog by remember { mutableStateOf(false) }
    var editingAssessment by remember { mutableStateOf<AssessmentRecord?>(null) }

    val visibleAssessments = remember(snapshot.assessments, reportScope, snapshot.selectedClassId) {
        when (reportScope) {
            "KELAS" -> snapshot.assessments.filter { it.classId == snapshot.selectedClassId }
            "KELOMPOK" -> snapshot.assessments.filter { it.groupName.contains("Alpha", ignoreCase = true) }
            "INDIVIDU" -> snapshot.assessments.take(2)
            else -> snapshot.assessments
        }
    }

    fun exportCsvReport() {
        val header = "Siswa,Kelas,Kelompok,Level,Misi,Durasi(m),Teknis,Desain,Fisik,Troubleshooting,NilaiAkhir,Status,CatatanGuru\n"
        val body = visibleAssessments.joinToString("\n") { a ->
            "\"${a.studentName}\",${a.classId},\"${a.groupName}\",${a.levelNumber},\"${a.missionTitle}\",${a.durationMinutes},${a.technicalScore},${a.designScore},${a.physicalScore},${a.troubleshootingScore},${a.finalScore},${a.status},\"${a.teacherNote.replace("\"", "'")}\""
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Penilaian NET4CITY ($reportScope)")
            putExtra(Intent.EXTRA_TEXT, header + body)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan / Unduh Laporan CSV NET4CITY"))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Penilaian Rubrik") },
                icon = { Icon(Icons.Rounded.Grading, contentDescription = null) },
                modifier = Modifier.testTag("tab_assessment")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Laporan & Analitik") },
                icon = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
                modifier = Modifier.testTag("tab_reports")
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (canGrade) {
                    item {
                        Button(
                            onClick = {
                                editingAssessment = null
                                showGradeDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_assessment_button")
                        ) {
                            Icon(Icons.Rounded.AddTask, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Input Penilaian Siswa Baru")
                        }
                    }
                } else {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Mode Siswa (Hanya Baca): Nilai diberikan dan dikunci oleh Guru Pengampu. Siswa tidak dapat mengubah nilai.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                items(snapshot.assessments, key = { it.id }) { a ->
                    val isFinalized = a.status == "FINALIZED"
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
                                    Text(a.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${a.classId.uppercase().replace("_", " ")} • ${a.groupName} • Lv.${a.levelNumber}: ${a.missionTitle}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (a.finalScore >= 85) NetEmeraldContainer else NetAmberContainer
                                ) {
                                    Text(
                                        text = "${a.finalScore}/100",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (a.finalScore >= 85) NetOnEmeraldContainer else NetOnAmberContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ScoreMiniChip("Teknis", a.technicalScore.toInt())
                                ScoreMiniChip("Desain", a.designScore.toInt())
                                ScoreMiniChip("Fisik", a.physicalScore.toInt())
                                ScoreMiniChip("Troubleshoot", a.troubleshootingScore.toInt())
                            }

                            Text(
                                text = "Hasil Game: ${a.gameResultSummary} • Waktu: ${a.durationMinutes} mnt • Diorama: ${if (a.dioramaVerified) "Terverifikasi" else "Pending"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (a.teacherNote.isNotBlank()) {
                                Text(
                                    text = "Catatan Guru: \"${a.teacherNote}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(if (isFinalized) "FINALIZED (Terkunci)" else "DRAFT") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isFinalized) Icons.Rounded.Lock else Icons.Rounded.EditNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                                if (canGrade && (!isFinalized || snapshot.activePersona == WorkspacePersona.ADMIN)) {
                                    TextButton(
                                        onClick = {
                                            editingAssessment = a
                                            showGradeDialog = true
                                        }
                                    ) {
                                        Text("Edit / Finalisasi")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Reports & Analytics Tab
            val avgTech = visibleAssessments.map { it.technicalScore }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
            val avgDesign = visibleAssessments.map { it.designScore }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
            val avgPhys = visibleAssessments.map { it.physicalScore }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
            val avgTrouble = visibleAssessments.map { it.troubleshootingScore }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
            val overallAvg = visibleAssessments.map { it.finalScore }.average().takeIf { !it.isNaN() }?.toInt() ?: 0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("KESELURUHAN", "KELAS", "KELOMPOK", "INDIVIDU").forEach { scope ->
                            FilterChip(
                                selected = reportScope == scope,
                                onClick = { reportScope = scope },
                                label = { Text(scope) }
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Ringkasan Kompetensi ($reportScope) • Rata-rata $overallAvg/100",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            RubricBar("Pengetahuan Teknis", avgTech, NetCyanPrimary)
                            RubricBar("Kemampuan Desain", avgDesign, NetPurpleAccent)
                            RubricBar("Praktik Fisik Diorama", avgPhys, NetEmerald)
                            RubricBar("Troubleshooting", avgTrouble, NetAmber)

                            Button(
                                onClick = { exportCsvReport() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_csv_report_button")
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ekspor / Bagikan Laporan CSV (Excel)")
                            }
                        }
                    }
                }

                // Class Comparison Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Perbandingan Progres Antar Kelas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            snapshot.classes.forEach { cls ->
                                val clsGrades = snapshot.assessments.filter { it.classId == cls.id }
                                val clsAvg = clsGrades.map { it.finalScore }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${cls.code} (Lv.${cls.maxUnlockedLevel}/10)", style = MaterialTheme.typography.bodyMedium)
                                    Text("${clsGrades.size} nilai • Rata $clsAvg", style = MaterialTheme.typography.labelMedium, color = NetCyanPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGradeDialog) {
        var studentName by remember { mutableStateOf(editingAssessment?.studentName ?: (snapshot.classStudents.firstOrNull()?.fullName ?: "Raka Pratama")) }
        var classId by remember { mutableStateOf(editingAssessment?.classId ?: snapshot.selectedClassId) }
        var groupName by remember { mutableStateOf(editingAssessment?.groupName ?: "Kelompok Alpha") }
        var levelNum by remember { mutableIntStateOf(editingAssessment?.levelNumber?.toInt() ?: 1) }
        var tech by remember { mutableFloatStateOf((editingAssessment?.technicalScore ?: 85L).toFloat()) }
        var design by remember { mutableFloatStateOf((editingAssessment?.designScore ?: 85L).toFloat()) }
        var phys by remember { mutableFloatStateOf((editingAssessment?.physicalScore ?: 85L).toFloat()) }
        var trouble by remember { mutableFloatStateOf((editingAssessment?.troubleshootingScore ?: 80L).toFloat()) }
        var note by remember { mutableStateOf(editingAssessment?.teacherNote ?: "") }
        var dioramaOk by remember { mutableStateOf(editingAssessment?.dioramaVerified ?: true) }
        var finalize by remember { mutableStateOf(editingAssessment?.status == "FINALIZED") }

        val missionTitle = CurriculumCatalog.levels[(levelNum - 1).coerceIn(0, 9)].mainMission.take(80)

        AlertDialog(
            onDismissRequest = { showGradeDialog = false },
            title = { Text(if (editingAssessment == null) "Input Nilai Praktikum" else "Edit Nilai ${editingAssessment?.studentName}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Nama Siswa") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = classId, onValueChange = { classId = it }, label = { Text("Kelas") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Kelompok") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Text("Level $levelNum — Teknis: ${tech.toInt()} | Desain: ${design.toInt()} | Fisik: ${phys.toInt()} | Troubleshoot: ${trouble.toInt()}", style = MaterialTheme.typography.labelMedium)
                    Text("Pengetahuan Teknis (${tech.toInt()})", style = MaterialTheme.typography.labelSmall)
                    Slider(value = tech, onValueChange = { tech = it }, valueRange = 0f..100f)
                    Text("Kemampuan Desain (${design.toInt()})", style = MaterialTheme.typography.labelSmall)
                    Slider(value = design, onValueChange = { design = it }, valueRange = 0f..100f)
                    Text("Praktik Fisik Diorama (${phys.toInt()})", style = MaterialTheme.typography.labelSmall)
                    Slider(value = phys, onValueChange = { phys = it }, valueRange = 0f..100f)
                    Text("Troubleshooting (${trouble.toInt()})", style = MaterialTheme.typography.labelSmall)
                    Slider(value = trouble, onValueChange = { trouble = it }, valueRange = 0f..100f)

                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan Evaluasi Guru") })

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = dioramaOk, onCheckedChange = { dioramaOk = it })
                        Text("Susunan Diorama Fisik Terverifikasi", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = finalize, onCheckedChange = { finalize = it })
                        Text("Kunci Nilai Permanen (FINALIZED)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveAssessment(
                        existingId = editingAssessment?.id,
                        studentName = studentName,
                        classId = classId,
                        groupName = groupName,
                        levelNumber = levelNum,
                        missionTitle = missionTitle,
                        durationMinutes = 45,
                        technicalScore = tech.toInt(),
                        designScore = design.toInt(),
                        physicalScore = phys.toInt(),
                        troubleshootingScore = trouble.toInt(),
                        gameResultSummary = "Praktik Virtual & Diorama Level $levelNum",
                        dioramaVerified = dioramaOk,
                        teacherNote = note,
                        status = if (finalize) "FINALIZED" else "DRAFT"
                    )
                    showGradeDialog = false
                }) { Text("Simpan Nilai") }
            },
            dismissButton = {
                TextButton(onClick = { showGradeDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ScoreMiniChip(label: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$score", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RubricBar(label: String, value: Int, color: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("$value / 100", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}
