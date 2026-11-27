package com.example.netcity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.netcity.model.*
import com.example.netcity.viewmodel.NetCitySnapshot
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    snapshot: NetCitySnapshot,
    onNavigate: (String) -> Unit,
    onSelectClass: (String) -> Unit,
    onSwitchPersona: (WorkspacePersona) -> Unit,
    onReseedDemo: () -> Unit,
    onSignOut: () -> Unit
) {
    val persona = snapshot.activePersona
    val activeClass = snapshot.activeClass
    val activeWorld = snapshot.activeWorld
    val avgScore = remember(snapshot.assessments) {
        if (snapshot.assessments.isEmpty()) 0 else snapshot.assessments.map { it.finalScore }.average().toInt()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Welcome Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NetSkyCheerStart, NetSkyCheerMid, NetMintCheer)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        text = "NET4CITY",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = NetAmberBright.copy(alpha = 0.28f)
                                    ) {
                                        Text(
                                            text = persona.label.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NetAmberBright,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "GAME EDUKASI PEMBELAJARAN JARINGAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NetAmberBright,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalIconButton(
                                    onClick = { onNavigate(ROUTE_MANUAL) },
                                    modifier = Modifier.testTag("dashboard_manual_button"),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.24f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Rounded.MenuBook, contentDescription = "Panduan Penggunaan")
                                }
                                FilledTonalIconButton(
                                    onClick = onSignOut,
                                    modifier = Modifier.testTag("sign_out_button"),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Rounded.Logout, contentDescription = "Keluar")
                                }
                            }
                        }

                        Text(
                            text = when (persona) {
                                WorkspacePersona.ADMIN -> "Pusat Kendali Administrator — Kelola seluruh kelas, akun guru & siswa, perangkat barcode, diorama, dan laporan."
                                WorkspacePersona.GURU -> "Panel Guru Pengampu — Buka sesi praktikum PID, atur kelompok, verifikasi diorama fisik, dan berikan penilaian rubrik."
                                WorkspacePersona.SISWA -> "Halo, Calon Network Engineer! Selesaikan misi 10 level STEM, kustomisasi karakter, pindai diorama, dan raih XP."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f)
                        )

                        // Role Switcher for Testing All 3 Role Dashboards
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WorkspacePersona.entries.forEach { role ->
                                val selected = role == persona
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSwitchPersona(role) },
                                    label = { Text(role.label, fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (role) {
                                                WorkspacePersona.ADMIN -> Icons.Rounded.AdminPanelSettings
                                                WorkspacePersona.GURU -> Icons.Rounded.School
                                                WorkspacePersona.SISWA -> Icons.Rounded.Engineering
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("role_chip_${role.name.lowercase()}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NetAmberBright,
                                        selectedLabelColor = NetNavyDeep,
                                        selectedLeadingIconColor = NetNavyDeep,
                                        containerColor = Color.White.copy(alpha = 0.18f),
                                        labelColor = Color.White,
                                        iconColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Simulation Data Disclaimer & Class Selector Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Class, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Pilih Kelas Aktif (PID & Diorama Bersama)",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "DATA SIMULASI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        snapshot.classes.forEach { cls ->
                            val isSelected = cls.id == snapshot.selectedClassId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectClass(cls.id) },
                                label = { Text("${cls.code} (Lv.${cls.maxUnlockedLevel})") },
                                modifier = Modifier.testTag("class_chip_${cls.id}")
                            )
                        }
                    }

                    if (activeClass != null) {
                        Text(
                            text = "Guru: ${activeClass.teacherName} • Sesi Aktif: ${activeClass.activeSessionMission}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Role-specific KPI Metrics Grid
        item {
            val stats = when (persona) {
                WorkspacePersona.ADMIN -> listOf(
                    StatMetric("Guru", "${snapshot.teachers.size}", Icons.Rounded.School, NetCyanPrimary),
                    StatMetric("Siswa", "${snapshot.students.size}", Icons.Rounded.Groups, NetEmerald),
                    StatMetric("Kelas", "${snapshot.classes.size}", Icons.Rounded.MeetingRoom, NetAmber),
                    StatMetric("Karakter", "${snapshot.characters.size}", Icons.Rounded.FaceRetouchingNatural, NetPurpleAccent),
                    StatMetric("Barcode", "${snapshot.barcodes.size}", Icons.Rounded.QrCode2, NetUtpBlue),
                    StatMetric("Diorama", "${snapshot.dioramaModules.size}", Icons.Rounded.ViewInAr, NetEmerald),
                    StatMetric("Scan PID", "${snapshot.scanLogs.size}", Icons.Rounded.QrCodeScanner, NetCyanPrimary),
                    StatMetric("Rata Nilai", "$avgScore", Icons.Rounded.Grading, NetAmber)
                )
                WorkspacePersona.GURU -> listOf(
                    StatMetric("Kelas Aktif", activeClass?.code ?: "-", Icons.Rounded.MeetingRoom, NetCyanPrimary),
                    StatMetric("Siswa Kelas", "${snapshot.classStudents.size}", Icons.Rounded.Groups, NetEmerald),
                    StatMetric("Level Buka", "Lv.${activeClass?.maxUnlockedLevel ?: 1}/10", Icons.Rounded.LockOpen, NetAmber),
                    StatMetric("Rata Nilai", "$avgScore/100", Icons.Rounded.Assessment, NetPurpleAccent)
                )
                WorkspacePersona.SISWA -> listOf(
                    StatMetric("Level Kota", "Lv.${activeWorld?.activeLevel ?: 1}", Icons.Rounded.RocketLaunch, NetCyanPrimary),
                    StatMetric("Total XP", "${activeWorld?.xp ?: 0} XP", Icons.Rounded.Bolt, NetAmber),
                    StatMetric("Saldo Kota", "NC$${activeWorld?.coins ?: 0}", Icons.Rounded.Savings, NetEmerald),
                    StatMetric("Kesehatan Net", "${activeWorld?.networkHealth ?: 100}%", Icons.Rounded.NetworkCheck, NetUtpBlue)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { metric ->
                            StatCard(metric = metric, modifier = Modifier.weight(1f))
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Quick Action Modules Grid (Every button routes to a real implemented feature!)
        item {
            Text(
                text = "Akses Cepat Modul NET4CITY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val actions = listOf(
                QuickActionTile("Kota 2.5D & Misi", "Bangun jaringan & 10 Level STEM", Icons.Rounded.LocationCity, NetCyanPrimary, ROUTE_CITY),
                QuickActionTile("Scan Mode PID", "USB Barcode Scanner & Registry", Icons.Rounded.QrCodeScanner, NetEmerald, ROUTE_SCAN),
                QuickActionTile("Character Studio", "Editor karakter modular & QR ID", Icons.Rounded.Face, NetPurpleAccent, ROUTE_CHARACTER),
                QuickActionTile("Diorama Manager", "10 Modul fisik & verifikasi guru", Icons.Rounded.ViewInAr, NetAmber, ROUTE_DIORAMA),
                QuickActionTile("Kelas & Pengguna", "CRUD X TKJ 1-4, Guru, Siswa & CSV", Icons.Rounded.ManageAccounts, NetUtpBlue, ROUTE_MANAGEMENT),
                QuickActionTile("Penilaian & Laporan", "Rubrik 4 kategori & ekspor CSV", Icons.Rounded.BarChart, NetCoralError, ROUTE_ASSESSMENT)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                actions.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { tile ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(tile.route) }
                                    .testTag("quick_action_${tile.route}"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, tile.accent.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(tile.accent.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(tile.icon, contentDescription = tile.title, tint = tile.accent)
                                    }
                                    Text(
                                        text = tile.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tile.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(ROUTE_MANUAL) }
                        .testTag("quick_action_manual"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NetCyanPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = "Buku Panduan",
                                tint = NetCyanPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Buku Panduan Penggunaan NET4CITY",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tutorial lengkap 8 bab: Simulasi Kota 2.5D, USB Scanner PID, Diorama, Karakter, CSV & FAQ.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Buka panduan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 10 Learning Levels Progression Preview
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
                    val completedCount = activeWorld?.completedLevels?.size ?: 0
                    val unlockedMax = (activeClass?.maxUnlockedLevel ?: 1L).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Peta Progres 10 Level TJKT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completedCount / 10 Selesai",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { completedCount / 10f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                    )
                    CurriculumCatalog.levels.forEach { lvl ->
                        val isDone = activeWorld?.completedLevels?.contains(lvl.number.toLong()) == true
                        val isUnlocked = lvl.number <= unlockedMax || persona == WorkspacePersona.ADMIN
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDone) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable(enabled = isUnlocked) { onNavigate(ROUTE_CITY) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> NetEmerald
                                            isUnlocked -> NetCyanPrimary
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isDone -> Icons.Rounded.Check
                                        isUnlocked -> Icons.Rounded.PlayArrow
                                        else -> Icons.Rounded.Lock
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Level ${lvl.number}: ${lvl.title}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = lvl.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "+${lvl.rewardXp} XP",
                                style = MaterialTheme.typography.labelMedium,
                                color = NetAmber
                            )
                        }
                    }
                }
            }
        }

        // Admin Demo Seed / Reset Control
        if (persona == WorkspacePersona.ADMIN) {
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sinkronisasi Ulang Data Demo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Memulihkan 2 Guru, 4 Kelas, 20 Siswa, 10 Karakter, 20 Barcode & 10 Modul Diorama simulasi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = onReseedDemo,
                            modifier = Modifier.testTag("reseed_demo_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Demo")
                        }
                    }
                }
            }
        }
    }
}

private data class StatMetric(val label: String, val value: String, val icon: ImageVector, val color: Color)
private data class QuickActionTile(val title: String, val subtitle: String, val icon: ImageVector, val accent: Color, val route: String)

@Composable
private fun StatCard(metric: StatMetric, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(metric.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(metric.icon, contentDescription = metric.label, tint = metric.color)
            }
            Column {
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
