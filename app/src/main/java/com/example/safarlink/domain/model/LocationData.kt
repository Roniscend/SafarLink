package com.example.safarlink.domain.model
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String = "" // <--- Ensure this is 'address', not 'name'
)