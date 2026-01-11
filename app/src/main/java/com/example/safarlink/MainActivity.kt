package com.example.safarlink

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.safarlink.presentation.auth.login.LoginViewModel
import com.example.safarlink.presentation.home.HomeViewModel
import com.example.safarlink.presentation.navigation.AppNavigation
import com.example.safarlink.ui.theme.SafarLinkTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels() // Inject HomeViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Permission Launcher
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseLocation = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (fineLocation || coarseLocation) {
            syncLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Location Client (This is GMS Location, NOT Google Maps - works without API Key)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check permissions immediately
        checkLocationPermissions()

        setContent {
            SafarLinkTheme {
                val currentUser by loginViewModel.currentUser.collectAsState()

                // Determine start screen
                val startDestination = if (currentUser != null) "home" else "login"

                AppNavigation(startDestination = startDestination)
            }
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            syncLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    @SuppressLint("MissingPermission")
    private fun syncLocation() {
        // 1. Try to get the LAST known location first (Instant)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updateViewModelLocation(location)
                } else {
                    // 2. If last location is null, request a NEW update (Slower)
                    requestNewLocation()
                }
            }
            .addOnFailureListener {
                requestNewLocation()
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        // Use BALANCED_POWER_ACCURACY to avoid hanging if GPS is weak inside buildings
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updateViewModelLocation(location)
                } else {
                    // If still null, maybe default to a city center or show error
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateViewModelLocation(location: Location) {


        homeViewModel.onCurrentLocationFound(location.latitude, location.longitude)
    }
}