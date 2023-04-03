package com.example.deliverycartusingxml.di

import android.app.Activity
import com.example.deliverycartusingxml.geolocation.AddressFinder
import com.example.deliverycartusingxml.geolocation.LocationFinder
import com.example.deliverycartusingxml.home.CartViewModel
import com.example.deliverycartusingxml.maps.MapsViewModel
import com.example.deliverycartusingxml.repository.MapsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object ConfigurationClass {
    fun modules() = viewModelModule + commonModules
}

val viewModelModule = module {
    single { MapsViewModel(get()) }
    single { CartViewModel() }
}

val commonModules = module {
    single { MapsRepository(get()) }
    single { AddressFinder() }
    single { LocationFinder(androidContext() as Activity) }
}