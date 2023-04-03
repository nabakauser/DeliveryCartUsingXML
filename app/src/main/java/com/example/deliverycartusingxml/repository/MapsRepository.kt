package com.example.deliverycartusingxml.repository

import android.content.Context
import com.example.deliverycartusingxml.geolocation.AddressFinder

class MapsRepository (
    private val addressFinder: AddressFinder,
) {
    fun getAddress(
        latitude : Double,
        longitude : Double,
        context: Context
    ) = addressFinder.findAddressFromLatLng(latitude, longitude, context)
}