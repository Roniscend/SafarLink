package com.example.safarlink.presentation.home

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safarlink.domain.model.LocationData
import com.example.safarlink.domain.model.RideOption
import com.example.safarlink.domain.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RideRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _pickupLocation = MutableStateFlow<LocationData?>(null)
    val pickupLocation = _pickupLocation.asStateFlow()

    private val _dropLocation = MutableStateFlow<LocationData?>(null)
    val dropLocation = _dropLocation.asStateFlow()

    private val _rideOptions = MutableStateFlow<List<RideOption>>(emptyList())
    val rideOptions = _rideOptions.asStateFlow()

    private val _locationSuggestions = MutableStateFlow<List<LocationData>>(emptyList())
    val locationSuggestions = _locationSuggestions.asStateFlow()

    private var searchJob: Job? = null

    fun onCurrentLocationFound(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val addressText = if (!addresses.isNullOrEmpty()) addresses[0].getAddressLine(0) else "Lat: $lat, Lng: $lng"
                val loc = LocationData(lat, lng, addressText)
                _pickupLocation.value = loc
            } catch (e: Exception) {
                _pickupLocation.value = LocationData(lat, lng, "Current Location")
            }
        }
    }

    fun searchPlaces(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            _locationSuggestions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            try {
                val urlString = "https://nominatim.openstreetmap.org/search?q=$query&format=json&addressdetails=1&limit=5"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "SafarLinkApp")
                val data = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(data)
                val results = mutableListOf<LocationData>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    results.add(
                        LocationData(
                            latitude = item.getDouble("lat"),
                            longitude = item.getDouble("lon"),
                            address = item.getString("display_name")
                        )
                    )
                }
                _locationSuggestions.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _locationSuggestions.value = emptyList()
            }
        }
    }

    fun clearSuggestions() {
        _locationSuggestions.value = emptyList()
    }

    fun updatePickup(location: LocationData) {
        _pickupLocation.value = location
    }

    fun updateDrop(location: LocationData) {
        _dropLocation.value = location
    }

    fun generateFares() {
        val pickup = _pickupLocation.value
        val drop = _dropLocation.value
        if (pickup != null && drop != null) {
            _rideOptions.value = repository.getFares(pickup, drop)
        }
    }
}