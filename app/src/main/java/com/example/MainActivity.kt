package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DeviceAccessMode
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CompassScreen
import com.example.ui.screens.DashboardMapScreen
import com.example.ui.screens.ElevationScreen
import com.example.ui.screens.HeritageScreen
import com.example.ui.screens.HikingScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MarineScreen
import com.example.ui.screens.MineralScreen
import com.example.ui.screens.OfflineMapScreen
import com.example.ui.screens.PriorityZoneScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SosScreen
import com.example.ui.screens.TreasureScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GarminViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GarminFinderApp()
            }
        }
    }
}

@Composable
fun GarminFinderApp() {
    val navController = rememberNavController()
    val viewModel: GarminViewModel = viewModel()

    // Request GPS permissions on launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            // Location updates already triggered in ViewModel init
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val deviceAccessMode by viewModel.deviceAccessMode.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    fun navigateToRoute(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val bottomNavItems = listOf(
        Triple("dashboard", "GPS Radar", Icons.Default.Explore),
        Triple("qibla", "Kiblat", Icons.Default.CompassCalibration),
        Triple("history", "Riwayat", Icons.Default.History),
        Triple("settings", "Pengaturan", Icons.Default.Settings)
    )

    val desktopNavItems = listOf(
        Triple("dashboard", "GPS Radar", Icons.Default.Explore),
        Triple("hiking", "Pendakian", Icons.Default.Terrain),
        Triple("elevation", "Elevasi", Icons.Default.Landscape),
        Triple("weather", "Cuaca", Icons.Default.Cloud),
        Triple("qibla", "Kiblat", Icons.Default.CompassCalibration),
        Triple("offline_maps", "Peta Offline", Icons.Default.Map),
        Triple("history", "Riwayat", Icons.Default.History),
        Triple("settings", "Pengaturan", Icons.Default.Settings)
    )

    val showBottomBar = bottomNavItems.any { it.first == currentRoute }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkTacticalBackground)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.One -> { navigateToRoute("dashboard"); true }
                        Key.Two -> { navigateToRoute("hiking"); true }
                        Key.Three -> { navigateToRoute("elevation"); true }
                        Key.Four -> { navigateToRoute("weather"); true }
                        Key.Five -> { navigateToRoute("qibla"); true }
                        Key.Six -> { navigateToRoute("offline_maps"); true }
                        Key.Seven -> { navigateToRoute("history"); true }
                        Key.Eight -> { navigateToRoute("settings"); true }
                        Key.S -> {
                            if (currentRoute != "sos") navController.navigate("sos")
                            true
                        }
                        Key.L -> {
                            val nextMode = if (deviceAccessMode == DeviceAccessMode.LAPTOP) DeviceAccessMode.HANDHELD else DeviceAccessMode.LAPTOP
                            viewModel.setDeviceAccessMode(nextMode)
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        val screenMaxWidth = maxWidth
        // Laptop Mode is active either if user explicitly selected LAPTOP, or AUTO on wide screens
        val isLaptopLayout = when (deviceAccessMode) {
            DeviceAccessMode.LAPTOP -> true
            DeviceAccessMode.HANDHELD -> false
            DeviceAccessMode.AUTO -> screenMaxWidth >= 760.dp
        }

        if (isLaptopLayout) {
            // PC / LAPTOP WORKSTATION ACCESS MODE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkTacticalBackground)
            ) {
                // Top Laptop Workstation Status & Hotkey Deck
                LaptopWorkstationTopBar(
                    deviceAccessMode = deviceAccessMode,
                    onToggleMode = { newMode -> viewModel.setDeviceAccessMode(newMode) }
                )

                // Side Tactical Navigation Rail + Main Workstation Content Area
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(DarkTacticalBackground)
                ) {
                    val isWideRail = screenMaxWidth >= 760.dp
                    val railWidth = if (isWideRail) 80.dp else 58.dp

                    NavigationRail(
                        containerColor = DarkTacticalSurface,
                        contentColor = Color.White,
                        modifier = Modifier
                            .width(railWidth)
                            .fillMaxHeight()
                            .border(
                                width = 1.dp,
                                color = DarkTacticalBorder,
                                shape = RoundedCornerShape(0.dp)
                            ),
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(top = 10.dp, bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isWideRail) 40.dp else 34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GarminOrange.copy(alpha = 0.2f))
                                        .border(1.5.dp, GarminOrange, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = "Garmin Logo",
                                        tint = GarminOrange,
                                        modifier = Modifier.size(if (isWideRail) 24.dp else 20.dp)
                                    )
                                }
                                if (isWideRail) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "GARMIN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.2.sp,
                                            fontSize = 10.sp
                                        ),
                                        color = GarminOrange
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = GarminCyan.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "LAPTOP PC",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = GarminCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                desktopNavItems.forEachIndexed { index, (route, label, icon) ->
                                    val selected = currentRoute == route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateToRoute(route) },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = "$label [${index + 1}]",
                                                tint = if (selected) GarminOrange else Color(0xFF94A3B8),
                                                modifier = Modifier.size(if (isWideRail) 22.dp else 20.dp)
                                            )
                                        },
                                        label = if (isWideRail) {
                                            {
                                                Text(
                                                    text = label,
                                                    color = if (selected) GarminOrange else Color(0xFF94A3B8),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    maxLines = 1
                                                )
                                            }
                                        } else null,
                                        colors = NavigationRailItemDefaults.colors(
                                            indicatorColor = GarminOrange.copy(alpha = 0.25f)
                                        )
                                    )
                                }
                            }

                            // Bottom SOS Trigger on Rail
                            Column(
                                modifier = Modifier.padding(bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                NavigationRailItem(
                                    selected = currentRoute == "sos",
                                    onClick = {
                                        if (currentRoute != "sos") {
                                            navController.navigate("sos")
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "SOS Darurat [S]",
                                            tint = GarminRed,
                                            modifier = Modifier.size(if (isWideRail) 22.dp else 20.dp)
                                        )
                                    },
                                    label = if (isWideRail) {
                                        {
                                            Text(
                                                text = "SOS",
                                                color = GarminRed,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    } else null,
                                    colors = NavigationRailItemDefaults.colors(
                                        indicatorColor = GarminRed.copy(alpha = 0.25f)
                                    )
                                )
                            }
                        }
                    }

                    // Main Content View in Desktop/Laptop Layout
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        GarminNavHost(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            // Mobile Compact Handheld Layout with Top Switcher and Bottom Navigation Bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkTacticalBackground)
            ) {
                HandheldModeTopBar(
                    onSwitchToLaptop = { viewModel.setDeviceAccessMode(DeviceAccessMode.LAPTOP) }
                )

                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(DarkTacticalBackground),
                    containerColor = DarkTacticalBackground,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = DarkTacticalSurface,
                                contentColor = Color.White
                            ) {
                                bottomNavItems.forEach { (route, label, icon) ->
                                    val selected = currentRoute == route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateToRoute(route) },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (selected) GarminOrange else Color(0xFF94A3B8)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                color = if (selected) GarminOrange else Color(0xFF94A3B8),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = GarminOrange.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    GarminNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LaptopWorkstationTopBar(
    deviceAccessMode: DeviceAccessMode,
    onToggleMode: (DeviceAccessMode) -> Unit
) {
    Surface(
        color = Color(0xFF0A0F1D),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Laptop Workstation Identifier
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GarminCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GarminCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Laptop,
                            contentDescription = "Mode Laptop",
                            tint = GarminCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "MODE LAPTOP WORKSTATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                fontSize = 9.sp
                            ),
                            color = GarminCyan
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = GarminEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GarminEmerald.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(GarminEmerald)
                        )
                        Text(
                            text = "DESKTOP PC",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GarminEmerald
                        )
                    }
                }
            }

            // Right: Hotkey Guide & Switch to Handheld Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Keyboard Hotkeys",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "1-8: Menu | S: SOS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GarminOrange.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable {
                        onToggleMode(DeviceAccessMode.HANDHELD)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Ganti ke Mode Ponsel",
                            tint = GarminOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Mode Ponsel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            ),
                            color = GarminOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HandheldModeTopBar(
    onSwitchToLaptop: () -> Unit
) {
    Surface(
        color = Color(0xFF0A0F1D),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = GarminAmber,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "MODE PONSEL (HANDHELD)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = GarminAmber
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = GarminCyan.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GarminCyan),
                modifier = Modifier.clickable { onSwitchToLaptop() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Laptop,
                        contentDescription = null,
                        tint = GarminCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Aktifkan Mode Laptop 💻",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp
                        ),
                        color = GarminCyan
                    )
                }
            }
        }
    }
}

@Composable
fun GarminNavHost(
    navController: androidx.navigation.NavHostController,
    viewModel: GarminViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            DashboardMapScreen(
                viewModel = viewModel,
                onNavigateToScreen = { route -> navController.navigate(route) }
            )
        }
        composable("qibla") {
            QiblaScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("hiking") {
            HikingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("marine") {
            MarineScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("elevation") {
            ElevationScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("weather") {
            WeatherScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("treasure") {
            TreasureScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("mineral") {
            MineralScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("heritage") {
            HeritageScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("priority_zone") {
            PriorityZoneScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("sos") {
            SosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("offline_maps") {
            OfflineMapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("compass") {
            CompassScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
