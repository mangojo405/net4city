package com.example.netcity.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.netcity.data.attemptAutoSignIn
import com.example.netcity.data.onGoogleSignInClicked
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var isAutoChecking by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showManual by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isAutoChecking = false
                onAuthSuccess()
            },
            onUnauthenticated = {
                isAutoChecking = false
            },
            scope = scope
        )
    }

    if (showManual) {
        BackHandler { showManual = false }
        ManualScreen(onBack = { showManual = false })
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "city_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NetSkyCheerStart,
                        NetSkyCheerMid,
                        NetSkyCheerEnd,
                        Color(0xFFE0F7FA)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Animated Cheerful Isometric City Network Hero Backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.24f

            // Sunny playful sky glow & soft cloud rings
            drawCircle(
                color = NetAmberBright.copy(alpha = 0.22f),
                radius = w * 0.34f,
                center = Offset(w * 0.85f, h * 0.08f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = w * 0.28f,
                center = Offset(w * 0.14f, h * 0.12f)
            )

            // Crisp blueprint grid lines
            val gridColor = Color.White.copy(alpha = 0.2f)
            for (i in -5..5) {
                val offset = i * 48f
                drawLine(
                    color = gridColor,
                    start = Offset(cx + offset - 260f, cy - 120f + i * 24f),
                    end = Offset(cx + offset + 260f, cy + 140f + i * 24f),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = gridColor,
                    start = Offset(cx + offset + 260f, cy - 120f + i * 24f),
                    end = Offset(cx + offset - 260f, cy + 140f + i * 24f),
                    strokeWidth = 1.5f
                )
            }

            // Three cheerful colorful isometric city towers
            fun drawTower(baseX: Float, baseY: Float, width: Float, height: Float, accent: Color) {
                val top = Path().apply {
                    moveTo(baseX, baseY - height - width * 0.5f)
                    lineTo(baseX + width, baseY - height)
                    lineTo(baseX, baseY - height + width * 0.5f)
                    lineTo(baseX - width, baseY - height)
                    close()
                }
                val left = Path().apply {
                    moveTo(baseX - width, baseY - height)
                    lineTo(baseX, baseY - height + width * 0.5f)
                    lineTo(baseX, baseY + width * 0.5f)
                    lineTo(baseX - width, baseY)
                    close()
                }
                val right = Path().apply {
                    moveTo(baseX + width, baseY - height)
                    lineTo(baseX, baseY - height + width * 0.5f)
                    lineTo(baseX, baseY + width * 0.5f)
                    lineTo(baseX + width, baseY)
                    close()
                }
                drawPath(left, accent.copy(alpha = 0.55f))
                drawPath(right, accent.copy(alpha = 0.35f))
                drawPath(top, Color.White.copy(alpha = 0.85f))
                drawPath(top, accent, style = Stroke(width = 2.5f))
                drawPath(left, Color.White.copy(alpha = 0.85f), style = Stroke(width = 1.6f))
                drawPath(right, Color.White.copy(alpha = 0.85f), style = Stroke(width = 1.6f))
            }

            drawTower(cx - 110f, cy + 45f, 36f, 75f, NetEmeraldBright)
            drawTower(cx, cy + 25f, 46f, 120f, NetAmberBright)
            drawTower(cx + 115f, cy + 50f, 34f, 68f, NetPinkCheer)

            // Animated bright data packets traveling between towers
            val p1 = Offset(cx - 110f, cy + 45f)
            val p2 = Offset(cx, cy + 25f)
            val p3 = Offset(cx + 115f, cy + 50f)
            drawLine(Color.White.copy(alpha = 0.85f), p1, p2, strokeWidth = 3.5f)
            drawLine(NetAmberBright.copy(alpha = 0.9f), p2, p3, strokeWidth = 3.5f)

            val dotA = Offset(p1.x + (p2.x - p1.x) * pulse, p1.y + (p2.y - p1.y) * pulse)
            val dotB = Offset(p2.x + (p3.x - p2.x) * pulse, p2.y + (p3.y - p2.y) * pulse)
            drawCircle(NetAmberBright, radius = 7f, center = dotA)
            drawCircle(Color.White, radius = 7f, center = dotB)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.24f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color.White.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Hub,
                            contentDescription = null,
                            tint = NetAmberBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "GAME EDUKASI PEMBELAJARAN JARINGAN",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NET4CITY",
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "BUILD YOUR CITY. CONNECT YOUR WORLD.",
                    style = MaterialTheme.typography.labelLarge,
                    color = NetAmberBright,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Game edukasi pembelajaran jaringan dengan simulasi kota 2.5D, Character Studio, QR/Barcode Scanner PID, dan Diorama Fisik modular.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.96f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bright Cheerful Elevated Sign-In Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FeatureMiniPill(Icons.Rounded.AdminPanelSettings, "Admin", NetSkyCheerStart)
                            FeatureMiniPill(Icons.Rounded.School, "Guru", NetEmerald)
                            FeatureMiniPill(Icons.Rounded.Engineering, "Siswa", NetAmber)
                        }

                        HorizontalDivider(color = LightOutline)

                        Text(
                            text = "Masuk dengan Akun Google untuk menyinkronkan database kelas, peta kota 2.5D, karakter, barcode, dan penilaian secara aman di Cloud Firestore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        if (errorMessage != null) {
                            Surface(
                                color = NetCoralContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = NetCoralError,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            },
                            enabled = !isLoading && !isAutoChecking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("google_sign_in_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NetSkyCheerStart,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading || isAutoChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isAutoChecking) "Memeriksa sesi..." else "Menghubungkan...",
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(Icons.Rounded.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Masuk dengan Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showManual = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_manual_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, NetEmerald.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NetEmeraldContainer.copy(alpha = 0.45f),
                                contentColor = NetOnEmeraldContainer
                            )
                        ) {
                            Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Panduan Penggunaan Aplikasi",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Mendukung Papan Interaktif Digital (PID), Tablet & USB Barcode Scanner",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureMiniPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.14f))
                .border(1.5.dp, tint.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LightOnSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
