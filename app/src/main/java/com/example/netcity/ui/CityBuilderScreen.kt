package com.example.netcity.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.netcity.model.*
import com.example.netcity.viewmodel.NetCitySnapshot
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.ui.theme.*
import kotlin.math.hypot
import kotlin.math.roundToInt

private enum class CityToolMode(val label: String) {
    INSPECT("Inspeksi"),
    BUILD("Bangun"),
    CABLE_UTP("Kabel UTP"),
    CABLE_FIBER("Fiber Optik")
}

@Composable
fun CityBuilderScreen(
    snapshot: NetCitySnapshot,
    viewModel: NetCityViewModel
) {
    val world = snapshot.activeWorld
    val activeClass = snapshot.activeClass
    if (world == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentLevelNum = world.activeLevel.toInt().coerceIn(1, 10)
    val levelSpec = CurriculumCatalog.levels[currentLevelNum - 1]
    val maxUnlocked = if (snapshot.activePersona == WorkspacePersona.ADMIN) 10
    else (activeClass?.maxUnlockedLevel ?: 1L).toInt().coerceAtLeast(currentLevelNum)

    val nodes = remember(world.placedNodes) { world.parsedNodes() }
    val links = remember(world.cableLinks) { world.parsedLinks() }

    var toolMode by remember { mutableStateOf(CityToolMode.INSPECT) }
    var selectedBuildSpec by remember { mutableStateOf(CurriculumCatalog.buildableCatalog.first { it.type == "PC" }) }
    var cableStartNodeId by remember { mutableStateOf<String?>(null) }
    var inspectedNode by remember { mutableStateOf<CityNode?>(null) }
    var showStemMissionSheet by remember { mutableStateOf(false) }

    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val packetTransition = rememberInfiniteTransition(label = "packets")
    val packetProgress by packetTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "packet_t"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // HUD Top Status Bar (Coins, XP, Network Health, Active Level)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = NetNavyDeep)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LEVEL ${levelSpec.number}: ${levelSpec.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = NetCyanBright,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeClass?.code ?: "X TKJ 1"} • Fase ${StemPhase.fromRaw(world.stemPhase).title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    FilledTonalButton(
                        onClick = { showStemMissionSheet = true },
                        modifier = Modifier.testTag("open_stem_mission_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NetAmberBright,
                            contentColor = NetNavyDeep
                        )
                    ) {
                        Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Misi STEM", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HudPill(Icons.Rounded.Savings, "NC$${world.coins}", NetAmberBright)
                    HudPill(Icons.Rounded.Bolt, "${world.xp} XP", NetCyanBright)
                    HudPill(
                        Icons.Rounded.NetworkCheck,
                        "Net ${world.networkHealth}%",
                        if (world.activeFault.isBlank()) NetEmeraldBright else Color(0xFFF87171)
                    )
                    HudPill(Icons.Rounded.Hub, "${nodes.size} Node / ${links.size} Link", Color.White)
                }

                // Level Selector Strip (1 - 10)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurriculumCatalog.levels.forEach { lv ->
                        val unlocked = lv.number <= maxUnlocked
                        val done = world.completedLevels.contains(lv.number.toLong())
                        val selected = lv.number == currentLevelNum
                        FilterChip(
                            selected = selected,
                            enabled = unlocked,
                            onClick = {
                                viewModel.setCityLevelAndPhase(world, lv.number, StemPhase.EXPLORE)
                            },
                            label = {
                                Text("Lv.${lv.number}" + if (done) " ✓" else "")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NetCyanBright,
                                selectedLabelColor = NetNavyDeep,
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.04f),
                                disabledLabelColor = Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }

        // Active Simulated Fault Alert Banner
        if (world.activeFault.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Column {
                            Text("GANGGUAN JARINGAN TERDETEKSI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                            Text(world.activeFault, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Button(
                        onClick = { viewModel.triggerOrFixFault(world, levelSpec, fix = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = NetEmerald),
                        modifier = Modifier.testTag("fix_fault_button")
                    ) {
                        Text("Perbaiki")
                    }
                }
            }
        }

        // Interactive 2.5D Isometric City Canvas Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(370.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1B33))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val gridSize = 8

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("isometric_city_canvas")
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                zoom = (zoom * gestureZoom).coerceIn(0.65f, 1.85f)
                                panOffset = Offset(
                                    (panOffset.x + pan.x).coerceIn(-260f, 260f),
                                    (panOffset.y + pan.y).coerceIn(-180f, 180f)
                                )
                            }
                        }
                        .pointerInput(nodes, toolMode, selectedBuildSpec, cableStartNodeId, zoom, panOffset) {
                            detectTapGestures { tap ->
                                val tileW = 74f * zoom
                                val tileH = 38f * zoom
                                val originX = size.width * 0.5f + panOffset.x
                                val originY = size.height * 0.16f + panOffset.y

                                fun tileCenter(gx: Int, gy: Int): Offset {
                                    val sx = originX + (gx - gy) * (tileW / 2f)
                                    val sy = originY + (gx + gy) * (tileH / 2f)
                                    return Offset(sx, sy)
                                }

                                // Hit-test existing node first
                                val hitNode = nodes.minByOrNull { n ->
                                    val c = tileCenter(n.gridX, n.gridY)
                                    hypot(tap.x - c.x, tap.y - (c.y - 16f * zoom))
                                }?.takeIf { n ->
                                    val c = tileCenter(n.gridX, n.gridY)
                                    hypot(tap.x - c.x, tap.y - (c.y - 16f * zoom)) <= 34f * zoom
                                }

                                when (toolMode) {
                                    CityToolMode.INSPECT -> {
                                        if (hitNode != null) inspectedNode = hitNode
                                    }
                                    CityToolMode.BUILD -> {
                                        // Inverse isometric transform from tap to (gx, gy)
                                        val dx = tap.x - originX
                                        val dy = tap.y - originY
                                        val gx = ((dx / (tileW / 2f) + dy / (tileH / 2f)) / 2f).roundToInt()
                                        val gy = ((dy / (tileH / 2f) - dx / (tileW / 2f)) / 2f).roundToInt()
                                        if (gx in 0 until gridSize && gy in 0 until gridSize) {
                                            viewModel.placeCityNode(world, selectedBuildSpec, gx, gy)
                                        }
                                    }
                                    CityToolMode.CABLE_UTP, CityToolMode.CABLE_FIBER -> {
                                        if (hitNode != null) {
                                            val start = cableStartNodeId
                                            if (start == null) {
                                                cableStartNodeId = hitNode.nodeId
                                                viewModel.showMessage("Node awal dipilih: ${hitNode.label}. Ketuk node tujuan!")
                                            } else if (start != hitNode.nodeId) {
                                                val type = if (toolMode == CityToolMode.CABLE_FIBER) "FIBER" else "UTP"
                                                viewModel.connectCityNodes(world, start, hitNode.nodeId, type)
                                                cableStartNodeId = null
                                            } else {
                                                cableStartNodeId = null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val tileW = 74f * zoom
                    val tileH = 38f * zoom
                    val originX = size.width * 0.5f + panOffset.x
                    val originY = size.height * 0.16f + panOffset.y

                    fun isoPoint(gx: Int, gy: Int): Offset = Offset(
                        originX + (gx - gy) * (tileW / 2f),
                        originY + (gx + gy) * (tileH / 2f)
                    )

                    // 1. Draw Isometric Ground Tiles + Roads
                    for (gx in 0 until gridSize) {
                        for (gy in 0 until gridSize) {
                            val center = isoPoint(gx, gy)
                            val isRoad = gx == 4 || gy == 4
                            val tileFill = when {
                                isRoad -> Color(0xFF1E293B)
                                (gx + gy) % 2 == 0 -> Color(0xFF14354B)
                                else -> Color(0xFF112E42)
                            }
                            val path = Path().apply {
                                moveTo(center.x, center.y - tileH / 2f)
                                lineTo(center.x + tileW / 2f, center.y)
                                lineTo(center.x, center.y + tileH / 2f)
                                lineTo(center.x - tileW / 2f, center.y)
                                close()
                            }
                            drawPath(path, tileFill)
                            drawPath(path, NetCyanBright.copy(alpha = 0.18f), style = Stroke(width = 1.2f))
                            if (isRoad) {
                                drawCircle(NetAmberBright.copy(alpha = 0.35f), radius = 2.2f * zoom, center = center)
                            }
                        }
                    }

                    // 2. Draw Cable Links (UTP Blue & Fiber Glowing Cyan) + Moving Data Packets
                    val nodeMap = nodes.associateBy { it.nodeId }
                    links.forEach { link ->
                        val from = nodeMap[link.fromId] ?: return@forEach
                        val to = nodeMap[link.toId] ?: return@forEach
                        val p1 = isoPoint(from.gridX, from.gridY) - Offset(0f, 10f * zoom)
                        val p2 = isoPoint(to.gridX, to.gridY) - Offset(0f, 10f * zoom)
                        val isFiber = link.cableType == "FIBER"
                        val lineColor = when {
                            world.activeFault.isNotBlank() -> Color(0xFFEF4444)
                            isFiber -> NetFiberGlow
                            else -> Color(0xFF60A5FA)
                        }
                        if (isFiber) {
                            drawLine(lineColor.copy(alpha = 0.3f), p1, p2, strokeWidth = 9f * zoom, cap = StrokeCap.Round)
                        }
                        drawLine(lineColor, p1, p2, strokeWidth = (if (isFiber) 3.8f else 2.8f) * zoom, cap = StrokeCap.Round)

                        // Moving packet dot
                        val pkt = Offset(
                            p1.x + (p2.x - p1.x) * packetProgress,
                            p1.y + (p2.y - p1.y) * packetProgress
                        )
                        drawCircle(
                            color = if (world.activeFault.isNotBlank()) NetAmberBright else Color.White,
                            radius = 4f * zoom,
                            center = pkt
                        )
                    }

                    // 3. Draw 2.5D Extruded Isometric Buildings & Network Devices (Depth sorted)
                    nodes.sortedBy { it.gridX + it.gridY }.forEach { node ->
                        val c = isoPoint(node.gridX, node.gridY)
                        val (topColor, leftColor, rightColor, heightPx) = nodeVisualStyle(node.type)
                        val h = heightPx * zoom
                        val bw = tileW * 0.36f
                        val bh = tileH * 0.36f

                        val topFace = Path().apply {
                            moveTo(c.x, c.y - h - bh)
                            lineTo(c.x + bw, c.y - h)
                            lineTo(c.x, c.y - h + bh)
                            lineTo(c.x - bw, c.y - h)
                            close()
                        }
                        val leftFace = Path().apply {
                            moveTo(c.x - bw, c.y - h)
                            lineTo(c.x, c.y - h + bh)
                            lineTo(c.x, c.y + bh)
                            lineTo(c.x - bw, c.y)
                            close()
                        }
                        val rightFace = Path().apply {
                            moveTo(c.x + bw, c.y - h)
                            lineTo(c.x, c.y - h + bh)
                            lineTo(c.x, c.y + bh)
                            lineTo(c.x + bw, c.y)
                            close()
                        }
                        drawPath(leftFace, leftColor)
                        drawPath(rightFace, rightColor)
                        drawPath(topFace, topColor)
                        drawPath(topFace, Color.White.copy(alpha = 0.55f), style = Stroke(1.3f))

                        // Selection ring if chosen as cable start
                        if (cableStartNodeId == node.nodeId) {
                            drawCircle(
                                color = NetAmberBright,
                                radius = 24f * zoom,
                                center = c - Offset(0f, h * 0.5f),
                                style = Stroke(width = 3f)
                            )
                        }

                        // Glowing LED indicator on network equipment
                        val ledColor = if (world.activeFault.isNotBlank()) Color(0xFFEF4444) else NetEmeraldBright
                        drawCircle(ledColor, radius = 3.5f * zoom, center = Offset(c.x, c.y - h))
                    }
                }

                // Floating Map Controls (Zoom & Fault Sim)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { zoom = (zoom + 0.18f).coerceAtMost(1.85f) },
                        containerColor = NetNavyElevated,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Perbesar peta")
                    }
                    SmallFloatingActionButton(
                        onClick = { zoom = (zoom - 0.18f).coerceAtLeast(0.65f) },
                        containerColor = NetNavyElevated,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Perkecil peta")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            zoom = 1f
                            panOffset = Offset.Zero
                        },
                        containerColor = NetNavyElevated,
                        contentColor = NetCyanBright
                    ) {
                        Icon(Icons.Rounded.CenterFocusStrong, contentDescription = "Reset posisi peta")
                    }
                }

                // Bottom-left Helper Hint Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = NetNavyDeep.copy(alpha = 0.88f)
                ) {
                    Text(
                        text = when (toolMode) {
                            CityToolMode.INSPECT -> "Mode Inspeksi: Ketuk bangunan/perangkat untuk detail teknis"
                            CityToolMode.BUILD -> "Mode Bangun: Ketuk tile kosong untuk pasang ${selectedBuildSpec.label}"
                            CityToolMode.CABLE_UTP -> "Kabel UTP (NC$40): Ketuk 2 perangkat untuk menghubungkan"
                            CityToolMode.CABLE_FIBER -> "Fiber Optik (NC$120): Ketuk 2 perangkat untuk jalur optik"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Toolbar Mode Switcher + Fault Simulator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CityToolMode.entries.forEach { mode ->
                FilterChip(
                    selected = toolMode == mode,
                    onClick = {
                        toolMode = mode
                        cableStartNodeId = null
                    },
                    label = { Text(mode.label) },
                    modifier = Modifier.testTag("city_mode_${mode.name.lowercase()}")
                )
            }
            AssistChip(
                onClick = { viewModel.triggerOrFixFault(world, levelSpec, fix = false) },
                label = { Text("Simulasi Gangguan") },
                leadingIcon = { Icon(Icons.Rounded.BugReport, contentDescription = null, tint = NetCoralError) },
                modifier = Modifier.testTag("trigger_fault_button")
            )
        }

        // Virtual Build & Device Catalog Strip
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
                    Text(
                        text = "Katalog Bangunan & Perangkat Jaringan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Inventory Scan: ${world.sessionInventory.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = NetEmerald
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurriculumCatalog.buildableCatalog.forEach { item ->
                        val isSelected = selectedBuildSpec.type == item.type && toolMode == CityToolMode.BUILD
                        Card(
                            modifier = Modifier
                                .width(136.dp)
                                .clickable {
                                    selectedBuildSpec = item
                                    toolMode = CityToolMode.BUILD
                                }
                                .testTag("build_item_${item.type.lowercase()}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(item.label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                                Text(item.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("NC$${item.price}", style = MaterialTheme.typography.labelMedium, color = NetAmber, fontWeight = FontWeight.Bold)
                                    Text("${item.ports}P", style = MaterialTheme.typography.labelSmall, color = NetCyanPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active STEM Mission Summary & Complete Action
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Misi Utama: ${levelSpec.mainMission}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Praktek Diorama: ${levelSpec.dioramaPractice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStemMissionSheet = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Buka Panduan STEM")
                    }
                    Button(
                        onClick = { viewModel.completeCurrentLevelMission(world, levelSpec, activeClass) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("complete_level_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NetEmerald)
                    ) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Selesaikan Level")
                    }
                }
            }
        }
    }

    // Technical Device Inspector Modal
    if (inspectedNode != null) {
        val node = inspectedNode!!
        val spec = CurriculumCatalog.buildableCatalog.firstOrNull { it.type == node.type }
        val connectedCount = links.count { it.fromId == node.nodeId || it.toId == node.nodeId }
        AlertDialog(
            onDismissRequest = { inspectedNode = null },
            title = { Text("${node.label} (${node.type})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Fungsi: ${spec?.functionDesc ?: "Objek kota jaringan"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Spesifikasi: ${spec?.specs ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Jumlah Port: ${spec?.ports ?: 1} Port • Tautan Aktif: $connectedCount", style = MaterialTheme.typography.labelMedium, color = NetCyanPrimary)
                    Text("Harga Virtual: NC$${spec?.price ?: 0}", style = MaterialTheme.typography.labelMedium, color = NetAmber)
                    Text("Kebutuhan Pemasangan: ${spec?.installNeeds ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Misi Terkait: Level ${spec?.minLevel ?: 1} • Barcode: ${node.barcodeCode}", style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = {
                Button(onClick = { inspectedNode = null }) { Text("Tutup") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.removeCityNode(world, node.nodeId)
                        inspectedNode = null
                    }
                ) {
                    Text("Bongkar Objek", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // 4-Phase STEM Mission & Math Engineering Dialog
    if (showStemMissionSheet) {
        var selectedAnswer by remember { mutableIntStateOf(-1) }
        val currentPhase = StemPhase.fromRaw(world.stemPhase)

        AlertDialog(
            onDismissRequest = { showStemMissionSheet = false },
            title = { Text("STEM Level ${levelSpec.number}: ${levelSpec.title}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(levelSpec.story, style = MaterialTheme.typography.bodySmall)
                    Text("Materi: ${levelSpec.topics.joinToString(" • ")}", style = MaterialTheme.typography.labelMedium, color = NetCyanPrimary)

                    // 4 STEM Phase Stepper
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StemPhase.entries.forEach { phase ->
                            FilterChip(
                                selected = currentPhase == phase,
                                onClick = { viewModel.setCityLevelAndPhase(world, levelSpec.number, phase) },
                                label = { Text(phase.title) }
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Science: ${levelSpec.stemScience}", style = MaterialTheme.typography.bodySmall)
                            Text("Technology: ${levelSpec.stemTechnology}", style = MaterialTheme.typography.bodySmall)
                            Text("Engineering: ${levelSpec.stemEngineering}", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Text("Mathematics & Keputusan Teknis:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(levelSpec.stemMathPrompt, style = MaterialTheme.typography.bodySmall)

                    levelSpec.stemMathOptions.forEachIndexed { idx, opt ->
                        val isPicked = selectedAnswer == idx
                        val isCorrect = idx == levelSpec.stemMathCorrectIndex
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAnswer = idx },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = when {
                                    isPicked && isCorrect -> NetEmeraldContainer
                                    isPicked && !isCorrect -> NetCoralContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (selectedAnswer != -1) {
                        val correct = selectedAnswer == levelSpec.stemMathCorrectIndex
                        Text(
                            text = if (correct) "Analisis perhitungan tepat! Lanjutkan ke fase BUILD pada peta 2.5D dan diorama fisik."
                            else "Periksa kembali rumus perhitungan teknis di atas dan coba lagi.",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (correct) NetEmerald else NetCoralError
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showStemMissionSheet = false }) { Text("Mulai Bangun") }
            }
        )
    }
}

private data class IsoStyle(val top: Color, val left: Color, val right: Color, val height: Float)

private fun nodeVisualStyle(type: String): IsoStyle = when (type) {
    "RUMAH" -> IsoStyle(Color(0xFFF59E0B), Color(0xFFB45309), Color(0xFFD97706), 24f)
    "SEKOLAH" -> IsoStyle(Color(0xFF38BDF8), Color(0xFF0369A1), Color(0xFF0284C7), 34f)
    "LAB" -> IsoStyle(Color(0xFF22D3EE), Color(0xFF0E7490), Color(0xFF0891B2), 30f)
    "KANTOR" -> IsoStyle(Color(0xFFA78BFA), Color(0xFF5B21B6), Color(0xFF7C3AED), 44f)
    "RS" -> IsoStyle(Color(0xFFF87171), Color(0xFFB91C1C), Color(0xFFDC2626), 42f)
    "PC" -> IsoStyle(Color(0xFF60A5FA), Color(0xFF1D4ED8), Color(0xFF2563EB), 16f)
    "SWITCH" -> IsoStyle(Color(0xFF34D399), Color(0xFF047857), Color(0xFF059669), 14f)
    "AP" -> IsoStyle(Color(0xFF2DD4BF), Color(0xFF0F766E), Color(0xFF0D9488), 20f)
    "SERVER" -> IsoStyle(Color(0xFF818CF8), Color(0xFF3730A3), Color(0xFF4338CA), 36f)
    "ROUTER" -> IsoStyle(Color(0xFFFBBF24), Color(0xFF92400E), Color(0xFFB45309), 22f)
    "DATACENTER" -> IsoStyle(Color(0xFF00E5FF), Color(0xFF004D61), Color(0xFF007B99), 52f)
    "POP" -> IsoStyle(Color(0xFF10B981), Color(0xFF064E3B), Color(0xFF047857), 48f)
    "ODC" -> IsoStyle(Color(0xFF94A3B8), Color(0xFF334155), Color(0xFF475569), 26f)
    "ODP" -> IsoStyle(Color(0xFF38BDF8), Color(0xFF1E3A8A), Color(0xFF1D4ED8), 28f)
    else -> IsoStyle(NetCyanBright, NetNavyElevated, NetCyanPrimary, 22f)
}

@Composable
private fun HudPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}
