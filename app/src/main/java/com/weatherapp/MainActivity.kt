package com.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weatherapp.model.MainViewModel
import com.weatherapp.ui.CityDialog
import com.weatherapp.ui.nav.BottomNavBar
import com.weatherapp.ui.nav.BottomNavItem
import com.weatherapp.ui.nav.MainNavHost
import com.weatherapp.ui.nav.Route
import com.weatherapp.ui.theme.WeatherAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.weatherapp.api.WeatherService
import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.model.MainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val navController = rememberNavController()

            val fbDB = remember { FBDatabase() }
            val weatherService = remember { WeatherService(this) }

            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(fbDB, weatherService)
            )

            var showDialog by remember { mutableStateOf(false) }

            val currentRoute = navController.currentBackStackEntryAsState()
            val showButton = currentRoute.value?.destination?.route == Route.ListScreen.route

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {}
            )

            WeatherAppTheme {

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                val name = viewModel.user?.name ?: "[carregando...]"
                                Text("Bem-vindo/a! $name")
                            },
                            actions = {
                                IconButton(onClick = {
                                    Firebase.auth.signOut()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sair"
                                    )
                                }
                            }
                        )
                    },

                    bottomBar = {
                        val items = listOf(
                            BottomNavItem.HomeButton,
                            BottomNavItem.ListButton,
                            BottomNavItem.MapButton,
                        )
                        BottomNavBar(viewModel, navController, items)
                    },

                    floatingActionButton = {
                        if (showButton) {
                            FloatingActionButton(
                                onClick = { showDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                            }
                        }
                    }


                ) { innerPadding ->

                    LaunchedEffect(Unit) {
                        launcher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                    Box(modifier = Modifier.padding(innerPadding)) {



                        MainNavHost(
                            navController = navController,
                            viewModel = viewModel
                        )

                        if (showDialog) {
                            CityDialog(
                                onDismiss = { showDialog = false },
                                onConfirm = { city ->
                                    if (city.isNotBlank()) {
                                        viewModel.addCity(city)
                                    }
                                    showDialog = false
                                }
                            )
                        }
                        LaunchedEffect(viewModel.page) {

                            navController.navigate(viewModel.page.route) {

                                navController.graph.startDestinationRoute?.let {
                                    popUpTo(it) {
                                        saveState = true
                                    }
                                }

                                restoreState = true
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }
    }
}