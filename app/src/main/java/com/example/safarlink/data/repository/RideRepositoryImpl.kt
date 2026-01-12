package com.example.safarlink.data.repository

import com.example.safarlink.domain.model.LocationData
import com.example.safarlink.domain.model.RideOption
import com.example.safarlink.domain.repository.RideRepository
import javax.inject.Inject
import kotlin.math.abs

class RideRepositoryImpl @Inject constructor() : RideRepository {

    override fun getFares(pickup: LocationData, drop: LocationData): List<RideOption> {

        val options = mutableListOf<RideOption>()

        options.add(
            RideOption(
                id = "rapido",
                providerName = "Rapido",
                packageName = "com.rapido.passenger",
                deepLinkUri = "PACKAGE:com.rapido.passenger",
                price = 0,
                eta = ""
            )
        )

        options.add(
            RideOption(
                id = "ola",
                providerName = "Ola Cabs",
                packageName = "com.olacabs.customer",
                deepLinkUri = "olacabs://app/launch?lat=${pickup.latitude}&lng=${pickup.longitude}&drop_lat=${drop.latitude}&drop_lng=${drop.longitude}&category=share",
                price = 0,
                eta = ""
            )
        )

        options.add(
            RideOption(
                id = "uber",
                providerName = "Uber",
                packageName = "com.ubercab",
                deepLinkUri = "uber://?action=setPickup&pickup[latitude]=${pickup.latitude}&pickup[longitude]=${pickup.longitude}&dropoff[latitude]=${drop.latitude}&dropoff[longitude]=${drop.longitude}",
                price = 0,
                eta = ""
            )
        )

        if (isBangalore(pickup.latitude, pickup.longitude) || isBangalore(drop.latitude, drop.longitude)) {
            options.add(
                RideOption(
                    id = "namma_yatri",
                    providerName = "Namma Yatri",
                    packageName = "in.juspay.nammayatri",
                    deepLinkUri = "PACKAGE:in.juspay.nammayatri",
                    price = 0,
                    eta = ""
                )
            )
        }

        return options
    }


    private fun isBangalore(lat: Double, lng: Double): Boolean {
        val bangaloreLat = 12.9716
        val bangaloreLng = 77.5946
        return abs(lat - bangaloreLat) < 0.5 && abs(lng - bangaloreLng) < 0.5
    }
}