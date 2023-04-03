package com.example.deliverycartusingxml.geolocation

import android.content.Context
import android.location.Geocoder
import android.util.Log
import java.util.*

class AddressFinder(
) {

    fun findAddressFromLatLng(
        latitude : Double,
        longitude : Double,
        context: Context
    ) : String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val address = if (addresses != null && addresses.isNotEmpty()) addresses[0] else null
        Log.e("address", "findAddressFromLatLng: ${address?.getAddressLine(0)}", )
        return address?.getAddressLine(0) ?: ""
    }
}