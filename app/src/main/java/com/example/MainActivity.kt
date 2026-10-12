package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.BacktestScreen
import com.example.ui.screens.ChartScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.RiskCalculatorScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignalDetailsScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle Android system back button when not on dashboard
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = SurfaceDark,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Terminal",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Terminal", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SCANNER,
                    onClick = { viewModel.navigateTo(AppScreen.SCANNER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Scanner",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Scanner", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.CHART,
                    onClick = { viewModel.navigateTo(AppScreen.CHART) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Charts",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Charts", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.RISK_CALC,
                    onClick = { viewModel.navigateTo(AppScreen.RISK_CALC) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Risk",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Risk", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.JOURNAL,
                    onClick = { viewModel.navigateTo(AppScreen.JOURNAL) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Task,
                            contentDescription = "Journal",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Journal", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.ACADEMY,
                    onClick = { viewModel.navigateTo(AppScreen.ACADEMY) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Academy",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Academy", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.BACKTEST,
                    onClick = { viewModel.navigateTo(AppScreen.BACKTEST) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Backtest",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Backtest", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Settings", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SurfaceDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.SCANNER -> ScannerScreen(viewModel = viewModel)
                AppScreen.CHART -> ChartScreen(viewModel = viewModel)
                AppScreen.SIGNAL_DETAILS -> SignalDetailsScreen(viewModel = viewModel)
                AppScreen.RISK_CALC -> RiskCalculatorScreen(viewModel = viewModel)
                AppScreen.JOURNAL -> JournalScreen(viewModel = viewModel)
                AppScreen.BACKTEST -> BacktestScreen(viewModel = viewModel)
                AppScreen.ACADEMY -> AcademyScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
