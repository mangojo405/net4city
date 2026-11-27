package com.example.netcity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.netcity.data.QrCodeGenerator
import com.example.netcity.model.CurriculumCatalog
import com.example.netcity.model.NetCityCharacter
import com.example.netcity.viewmodel.NetCitySnapshot
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.ui.theme.*

@Composable
fun CharacterStudioScreen(
    snapshot: NetCitySnapshot,
    viewModel: NetCityViewModel
) {
    var selectedChar by remember(snapshot.characters) {
        mutableStateOf(snapshot.characters.firstOrNull())
    }
    var editingId by remember { mutableStateOf<String?>(selectedChar?.id) }
    var characterCode by remember { mutableStateOf(selectedChar?.characterCode ?: "NC-CHAR-00027") }
    var charName by remember { mutableStateOf(selectedChar?.name ?: "Teknisi Baru") }
    var ownerName by remember { mutableStateOf(selectedChar?.ownerName ?: "Raka Pratama") }
    var classId by remember { mutableStateOf(selectedChar?.classId ?: snapshot.selectedClassId) }
    var profession by remember { mutableStateOf(selectedChar?.profession ?: CurriculumCatalog.professions.first()) }
    var faceShape by remember { mutableStateOf(selectedChar?.faceShape ?: CurriculumCatalog.faceShapes.first()) }
    var hairStyle by remember { mutableStateOf(selectedChar?.hairStyle ?: CurriculumCatalog.hairStyles.first()) }
    var hairColor by remember { mutableStateOf(selectedChar?.hairColor ?: CurriculumCatalog.hairColors.first()) }
    var outfitStyle by remember { mutableStateOf(selectedChar?.outfitStyle ?: CurriculumCatalog.outfitStyles.first()) }
    var pantsColor by remember { mutableStateOf(selectedChar?.pantsColor ?: CurriculumCatalog.pantsColors.first()) }
    var shoeStyle by remember { mutableStateOf(selectedChar?.shoeStyle ?: CurriculumCatalog.shoeStyles.first()) }
    var accessory by remember { mutableStateOf(selectedChar?.accessory ?: CurriculumCatalog.accessories[1]) }
    var workTool by remember { mutableStateOf(selectedChar?.workTool ?: CurriculumCatalog.workTools.first()) }
    var showQrModal by remember { mutableStateOf(false) }

    fun loadCharacter(c: NetCityCharacter) {
        selectedChar = c
        editingId = c.id
        characterCode = c.characterCode
        charName = c.name
        ownerName = c.ownerName
        classId = c.classId
        profession = c.profession
        faceShape = c.faceShape
        hairStyle = c.hairStyle
        hairColor = c.hairColor
        outfitStyle = c.outfitStyle
        pantsColor = c.pantsColor
        shoeStyle = c.shoeStyle
        accessory = c.accessory
        workTool = c.workTool
    }

    val qrBitmap = remember(characterCode) { QrCodeGenerator.generateQrBitmap(characterCode, 340) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Existing Characters Carousel + New Character Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    editingId = null
                    characterCode = "NC-CHAR-%05d".format((100..99999).random())
                    charName = "Teknisi Baru"
                    ownerName = snapshot.classStudents.firstOrNull()?.fullName ?: "Siswa TKJ"
                    classId = snapshot.selectedClassId
                },
                modifier = Modifier.testTag("new_character_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Karakter Baru")
            }

            snapshot.characters.forEach { c ->
                FilterChip(
                    selected = editingId == c.id,
                    onClick = { loadCharacter(c) },
                    label = { Text("${c.name} (${c.characterCode.takeLast(5)})") }
                )
            }
        }

        // Live Modular Layered Character Stage + QR Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(NetSkyCheerStart, NetSkyCheerMid, NetMintCheer))
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modular Canvas Renderer
                    Box(
                        modifier = Modifier
                            .size(width = 138.dp, height = 178.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        LayeredCharacterCanvas(
                            faceShape = faceShape,
                            hairStyle = hairStyle,
                            hairColor = hairColor,
                            outfitStyle = outfitStyle,
                            pantsColor = pantsColor,
                            shoeStyle = shoeStyle,
                            accessory = accessory,
                            workTool = workTool,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.24f)
                        ) {
                            Text(
                                text = characterCode,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = charName.ifBlank { "Tanpa Nama" },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profession,
                            style = MaterialTheme.typography.labelLarge,
                            color = NetAmberBright,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pemilik: $ownerName • ${classId.uppercase().replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                        Text(
                            text = "Alat: $workTool",
                            style = MaterialTheme.typography.bodySmall,
                            color = NetAmberBright
                        )
                        Spacer(Modifier.height(4.dp))
                        FilledTonalButton(
                            onClick = { showQrModal = true },
                            modifier = Modifier.testTag("show_character_qr_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = NetAmberBright,
                                contentColor = NetNavyDeep
                            )
                        ) {
                            Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("QR ID Karakter", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Modular Layer Customizer Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Kustomisasi Layer Modular (Ubah Tanpa Buat Ulang)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = charName,
                        onValueChange = { charName = it },
                        label = { Text("Nama Karakter") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Pemilik (Siswa/Guru)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OptionSelectorRow("Profesi TJKT", CurriculumCatalog.professions, profession) { profession = it }
                OptionSelectorRow("Bentuk Wajah", CurriculumCatalog.faceShapes, faceShape) { faceShape = it }
                OptionSelectorRow("Gaya Rambut", CurriculumCatalog.hairStyles, hairStyle) { hairStyle = it }
                OptionSelectorRow("Warna Rambut", CurriculumCatalog.hairColors, hairColor) { hairColor = it }
                OptionSelectorRow("Seragam / Baju", CurriculumCatalog.outfitStyles, outfitStyle) { outfitStyle = it }
                OptionSelectorRow("Warna Celana", CurriculumCatalog.pantsColors, pantsColor) { pantsColor = it }
                OptionSelectorRow("Sepatu", CurriculumCatalog.shoeStyles, shoeStyle) { shoeStyle = it }
                OptionSelectorRow("Aksesori", CurriculumCatalog.accessories, accessory) { accessory = it }
                OptionSelectorRow("Peralatan Kerja", CurriculumCatalog.workTools, workTool) { workTool = it }

                Button(
                    onClick = {
                        viewModel.saveCharacter(
                            existingId = editingId,
                            characterCode = characterCode,
                            name = charName,
                            ownerName = ownerName,
                            classId = classId,
                            profession = profession,
                            faceShape = faceShape,
                            hairStyle = hairStyle,
                            hairColor = hairColor,
                            outfitStyle = outfitStyle,
                            pantsColor = pantsColor,
                            shoeStyle = shoeStyle,
                            accessory = accessory,
                            workTool = workTool,
                            skills = listOf("Perakitan PC", "Kabel UTP & Fiber", profession)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_character_button")
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan Karakter ke Cloud Firestore", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showQrModal) {
        AlertDialog(
            onDismissRequest = { showQrModal = false },
            title = { Text("QR Code Identitas Karakter") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code $characterCode",
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        )
                    }
                    Text(characterCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "QR ini hanya menyimpan ID permanen ($characterCode). Seluruh data layer & XP karakter tersimpan aman di Cloud Firestore.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showQrModal = false }) { Text("Selesai") }
            }
        )
    }
}

@Composable
private fun OptionSelectorRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
private fun LayeredCharacterCanvas(
    faceShape: String,
    hairStyle: String,
    hairColor: String,
    outfitStyle: String,
    pantsColor: String,
    shoeStyle: String,
    accessory: String,
    workTool: String,
    modifier: Modifier = Modifier
) {
    val hairTint = when (hairColor) {
        "Cokelat Tua" -> Color(0xFF4E342E)
        "Biru Cyber" -> NetCyanBright
        "Abu Perak" -> Color(0xFFCBD5E1)
        else -> Color(0xFF1E293B)
    }
    val outfitTint = when (outfitStyle) {
        "Seragam Siswa SMK TKJ" -> Color(0xFFF8FAFC)
        "Jaket ISP Lapangan" -> NetEmerald
        "Rompi Safety Fiber" -> NetAmberBright
        "Kemeja Server Admin" -> Color(0xFF4F46E5)
        else -> NetCyanPrimary
    }
    val pantsTint = when (pantsColor) {
        "Abu Gelap" -> Color(0xFF475569)
        "Khaki" -> Color(0xFFB49A6E)
        "Hitam" -> Color(0xFF0F172A)
        else -> Color(0xFF1E3A8A)
    }
    val shoeTint = when (shoeStyle) {
        "Sneakers Lab" -> NetCyanBright
        "Sepatu Pantofel" -> Color(0xFF111827)
        else -> Color(0xFF92400E)
    }

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val skinColor = Color(0xFFF3C6A5)

        // Floor shadow
        drawOval(
            color = NetCyanBright.copy(alpha = 0.22f),
            topLeft = Offset(cx - 42f, size.height * 0.86f),
            size = Size(84f, 16f)
        )

        // Shoes Layer
        drawRoundRect(shoeTint, Offset(cx - 26f, size.height * 0.78f), Size(20f, 14f), CornerRadius(6f, 6f))
        drawRoundRect(shoeTint, Offset(cx + 6f, size.height * 0.78f), Size(20f, 14f), CornerRadius(6f, 6f))

        // Pants Layer
        drawRoundRect(pantsTint, Offset(cx - 24f, size.height * 0.58f), Size(20f, 44f), CornerRadius(6f, 6f))
        drawRoundRect(pantsTint, Offset(cx + 4f, size.height * 0.58f), Size(20f, 44f), CornerRadius(6f, 6f))

        // Torso / Outfit Layer
        drawRoundRect(outfitTint, Offset(cx - 32f, size.height * 0.34f), Size(64f, 54f), CornerRadius(14f, 14f))
        // Reflective technician stripe
        drawRect(NetCyanBright.copy(alpha = 0.75f), Offset(cx - 32f, size.height * 0.51f), Size(64f, 5f))

        // Arms
        drawRoundRect(outfitTint, Offset(cx - 46f, size.height * 0.36f), Size(14f, 42f), CornerRadius(7f, 7f))
        drawRoundRect(outfitTint, Offset(cx + 32f, size.height * 0.36f), Size(14f, 42f), CornerRadius(7f, 7f))

        // Work Tool Layer (right hand)
        val toolColor = when {
            workTool.contains("Optical") || workTool.contains("Fusion") -> NetAmberBright
            workTool.contains("Laptop") -> Color(0xFF94A3B8)
            else -> NetCoralError
        }
        drawRoundRect(toolColor, Offset(cx + 34f, size.height * 0.54f), Size(20f, 14f), CornerRadius(4f, 4f))

        // Head / Face Shape Layer
        val faceRadius = when (faceShape) {
            "Persegi" -> 12f
            "Tegas" -> 16f
            else -> 26f
        }
        drawRoundRect(skinColor, Offset(cx - 24f, size.height * 0.13f), Size(48f, 46f), CornerRadius(faceRadius, faceRadius))

        // Hair Layer
        if (hairStyle == "Berhijab Rapi") {
            drawRoundRect(hairTint, Offset(cx - 28f, size.height * 0.10f), Size(56f, 52f), CornerRadius(24f, 24f))
            drawRoundRect(skinColor, Offset(cx - 19f, size.height * 0.17f), Size(38f, 34f), CornerRadius(16f, 16f))
        } else {
            val hairH = if (hairStyle == "Spiky Tech") 22f else 18f
            drawRoundRect(hairTint, Offset(cx - 26f, size.height * 0.10f), Size(52f, hairH), CornerRadius(14f, 14f))
        }

        // Eyes & Smile
        drawCircle(Color(0xFF0F172A), 3.5f, Offset(cx - 9f, size.height * 0.25f))
        drawCircle(Color(0xFF0F172A), 3.5f, Offset(cx + 9f, size.height * 0.25f))
        drawRoundRect(Color(0xFF7F1D1D), Offset(cx - 7f, size.height * 0.31f), Size(14f, 3.5f), CornerRadius(2f, 2f))

        // Accessory Layer
        when {
            accessory.contains("Kacamata") -> {
                drawRoundRect(NetCyanBright, Offset(cx - 17f, size.height * 0.22f), Size(14f, 9f), CornerRadius(3f, 3f), alpha = 0.65f)
                drawRoundRect(NetCyanBright, Offset(cx + 3f, size.height * 0.22f), Size(14f, 9f), CornerRadius(3f, 3f), alpha = 0.65f)
            }
            accessory.contains("Helm") -> {
                drawRoundRect(NetAmberBright, Offset(cx - 29f, size.height * 0.06f), Size(58f, 20f), CornerRadius(14f, 14f))
            }
            accessory.contains("Headset") -> {
                drawCircle(NetCyanBright, 6f, Offset(cx - 26f, size.height * 0.24f))
                drawCircle(NetCyanBright, 6f, Offset(cx + 26f, size.height * 0.24f))
            }
            accessory.contains("ID Card") -> {
                drawRoundRect(Color.White, Offset(cx - 8f, size.height * 0.40f), Size(16f, 18f), CornerRadius(3f, 3f))
            }
        }
    }
}
