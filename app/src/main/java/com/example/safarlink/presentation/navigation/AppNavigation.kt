package com.example.safarlink.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safarlink.presentation.auth.login.LoginScreen
import com.example.safarlink.presentation.auth.signup.SignUpScreen
import com.example.safarlink.presentation.home.HomeScreen
import com.example.safarlink.presentation.home.HomeViewModel
import com.example.safarlink.presentation.map.MapPickerScreen
import com.example.safarlink.presentation.results.ResultsScreen

@Composable
fun AppNavigation(
    startDestination: String = "login"
) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()

    var isSelectingPickup by remember { mutableStateOf(true) }
    val pickup by homeViewModel.pickupLocation.collectAsState()
    val drop by homeViewModel.dropLocation.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // 1. LOGIN
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }

        // 2. SIGN UP
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 3. HOME (Your Light Theme Screen)
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenMap = { isPickup ->
                    isSelectingPickup = isPickup
                    navController.navigate("map")
                },
                onSearchClick = {
                    homeViewModel.generateFares()
                    navController.navigate("results")
                },
                onSignOut = {
                    // Sign out and go back to Login
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // 4. MAP
        composable("map") {
            val targetLocation = if (isSelectingPickup) pickup else drop
            MapPickerScreen(
                initialLat = targetLocation?.latitude ?: 12.9716,
                initialLng = targetLocation?.longitude ?: 77.5946,
                onLocationSelected = { location ->
                    if (isSelectingPickup) homeViewModel.updatePickup(location)
                    else homeViewModel.updateDrop(location)
                    navController.popBackStack()
                }
            )
        }

        // 5. RESULTS
        composable("results") {
            ResultsScreen(viewModel = homeViewModel)
        }
    }
}