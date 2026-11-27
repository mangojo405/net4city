package com.example.netcity.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.netcity.data.signOutFromNetCity
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.netcity.viewmodel.UiState

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_CITY = "city_builder"
const val ROUTE_SCAN = "scan_mode"
const val ROUTE_CHARACTER = "character_studio"
const val ROUTE_DIORAMA = "diorama_manager"
const val ROUTE_MANAGEMENT = "management"
const val ROUTE_ASSESSMENT = "assessment"
const val ROUTE_MANUAL = "manual"

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val primaryNavItems = listOf(
    NavDestinationItem(ROUTE_DASHBOARD, "Beranda", Icons.Rounded.Dashboard),
    NavDestinationItem(ROUTE_CITY, "Kota 2.5D", Icons.Rounded.LocationCity),
    NavDestinationItem(ROUTE_SCAN, "Scan PID", Icons.Rounded.QrCodeScanner),
    NavDestinationItem(ROUTE_CHARACTER, "Karakter", Icons.Rounded.Face),
    NavDestinationItem(ROUTE_DIORAMA, "Diorama", Icons.Rounded.ViewInAr)
)

private val allRailItems = primaryNavItems + listOf(
    NavDestinationItem(ROUTE_MANAGEMENT, "Kelas/User", Icons.Rounded.ManageAccounts),
    NavDestinationItem(ROUTE_ASSESSMENT, "Penilaian", Icons.Rounded.Assessment),
    NavDestinationItem(ROUTE_MANUAL, "Panduan", Icons.Rounded.MenuBook)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetCityShell(
    viewModel: NetCityViewModel,
    onSignedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val banner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val lastScan by viewModel.lastScanResult.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(banner) {
        val msg = banner
        if (msg != null) {
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearBanner()
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (currentRoute != ROUTE_DASHBOARD) {
                    TopAppBar(
                        title = {
                            val title = allRailItems.firstOrNull { it.route == currentRoute }?.label ?: "NET4CITY"
                            Text(text = "NET4CITY • $title", fontWeight = FontWeight.Bold)
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigateTo(ROUTE_DASHBOARD) }) {
                                Icon(Icons.Rounded.Home, contentDescription = "Kembali ke Beranda")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { navigateTo(ROUTE_MANAGEMENT) },
                                modifier = Modifier.testTag("top_nav_management")
                            ) {
                                Icon(Icons.Rounded.ManageAccounts, contentDescription = "Kelola Kelas & Pengguna")
                            }
                            IconButton(
                                onClick = { navigateTo(ROUTE_ASSESSMENT) },
                                modifier = Modifier.testTag("top_nav_assessment")
                            ) {
                                Icon(Icons.Rounded.Assessment, contentDescription = "Penilaian & Laporan")
                            }
                            IconButton(
                                onClick = { navigateTo(ROUTE_MANUAL) },
                                modifier = Modifier.testTag("top_nav_manual")
                            ) {
                                Icon(Icons.Rounded.MenuBook, contentDescription = "Panduan Penggunaan")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        primaryNavItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = { navigateTo(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag("bottom_nav_${item.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        header = {
                            Icon(
                                imageVector = Icons.Rounded.Hub,
                                contentDescription = "NET4CITY",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    ) {
                        allRailItems.forEach { item ->
                            NavigationRailItem(
                                selected = currentRoute == item.route,
                                onClick = { navigateTo(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag("rail_nav_${item.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (val ui = state) {
                        is UiState.Loading -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator()
                                Text("Menyinkronkan NET4CITY dengan Cloud Firestore...")
                            }
                        }
                        is UiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                Text("Gagal memuat data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(ui.message, style = MaterialTheme.typography.bodySmall)
                                Button(onClick = { viewModel.reseedDemoData() }) {
                                    Text("Coba Sinkron Ulang")
                                }
                            }
                        }
                        is UiState.Success -> {
                            val snapshot = ui.data
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_DASHBOARD,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(ROUTE_DASHBOARD) {
                                    DashboardScreen(
                                        snapshot = snapshot,
                                        onNavigate = ::navigateTo,
                                        onSelectClass = viewModel::selectClass,
                                        onSwitchPersona = viewModel::switchPersona,
                                        onReseedDemo = viewModel::reseedDemoData,
                                        onSignOut = {
                                            signOutFromNetCity(credentialManager, scope, onSignedOut)
                                        }
                                    )
                                }
                                composable(ROUTE_CITY) {
                                    CityBuilderScreen(snapshot = snapshot, viewModel = viewModel)
                                }
                                composable(ROUTE_SCAN) {
                                    BarcodeScanScreen(snapshot = snapshot, lastScanResult = lastScan, viewModel = viewModel)
                                }
                                composable(ROUTE_CHARACTER) {
                                    CharacterStudioScreen(snapshot = snapshot, viewModel = viewModel)
                                }
                                composable(ROUTE_DIORAMA) {
                                    DioramaManagerScreen(snapshot = snapshot, viewModel = viewModel)
                                }
                                composable(ROUTE_MANAGEMENT) {
                                    ManagementScreen(snapshot = snapshot, viewModel = viewModel)
                                }
                                composable(ROUTE_ASSESSMENT) {
                                    AssessmentReportScreen(snapshot = snapshot, viewModel = viewModel)
                                }
                                composable(ROUTE_MANUAL) {
                                    ManualScreen(onNavigate = ::navigateTo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
