package com.example.deliverycartusingxml.maps

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.deliverycartusingxml.repository.MapsRepository

class MapsViewModel (
    private val mapsRepository: MapsRepository
): ViewModel() {

     var address: String = ""

    fun getAddress(latitude: Double, longitude: Double, context: Context): String {
        return mapsRepository.getAddress(latitude, longitude, context)
    }

}
