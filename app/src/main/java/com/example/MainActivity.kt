package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GardenScreen
import com.example.ui.HabitViewModel
import com.example.ui.ProfileScreen
import com.example.ui.TodayScreen
import com.example.ui.TrendsScreen
import com.example.ui.theme.RootineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RootineTheme {
                RootineApp()
            }
        }
    }
}

enum class RootineTab {
    Today, Trends, Garden, Profile
}

@Composable
fun RootineApp() {
    val viewModel: HabitViewModel = viewModel()
    var currentTab by remember { mutableStateOf(RootineTab.Today) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == RootineTab.Today,
                    onClick = { currentTab = RootineTab.Today },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                    label = { Text("Today") },
                    modifier = Modifier.testTag("nav_tab_today")
                )
                NavigationBarItem(
                    selected = currentTab == RootineTab.Trends,
                    onClick = { currentTab = RootineTab.Trends },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Trends") },
                    label = { Text("Trends") },
                    modifier = Modifier.testTag("nav_tab_trends")
                )
                NavigationBarItem(
                    selected = currentTab == RootineTab.Garden,
                    onClick = { currentTab = RootineTab.Garden },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Garden") },
                    label = { Text("Garden") },
                    modifier = Modifier.testTag("nav_tab_garden")
                )
                NavigationBarItem(
                    selected = currentTab == RootineTab.Profile,
                    onClick = { currentTab = RootineTab.Profile },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            RootineTab.Today -> TodayScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            RootineTab.Trends -> TrendsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            RootineTab.Garden -> GardenScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            RootineTab.Profile -> ProfileScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
