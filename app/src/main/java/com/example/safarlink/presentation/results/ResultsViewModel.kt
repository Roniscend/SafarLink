package com.example.safarlink.presentation.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safarlink.domain.model.LocationData
import com.example.safarlink.domain.model.RideOption
import com.example.safarlink.domain.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: RideRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _rideOptions = MutableStateFlow<List<RideOption>>(emptyList())
    val rideOptions = _rideOptions.asStateFlow()

    fun calculateFares(
        pickupLat: Double,
        pickupLng: Double,
        dropLat: Double,
        dropLng: Double
    ) {
        viewModelScope.launch {
            // FIX: "name" changed to "address" to match LocationData.kt
            val pickupLocation = LocationData(
                latitude = pickupLat,
                longitude = pickupLng,
                address = "Pickup Location"
            )

            val dropLocation = LocationData(
                latitude = dropLat,
                longitude = dropLng,
                address = "Drop Location"
            )

            val options = repository.getFares(pickupLocation, dropLocation)
            _rideOptions.value = options
        }
    }
}