package com.example.safarlink.domain.repository

import com.example.safarlink.domain.model.LocationData
import com.example.safarlink.domain.model.RideOption

interface RideRepository {
    fun getFares(pickup: LocationData, drop: LocationData): List<RideOption>
}