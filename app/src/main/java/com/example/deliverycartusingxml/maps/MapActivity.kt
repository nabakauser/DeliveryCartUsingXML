package com.example.deliverycartusingxml.maps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.deliverycartusingxml.Constants.EXTRA_REQUEST_CODE
import com.example.deliverycartusingxml.Constants.KEY_ADDRESS
import com.example.deliverycartusingxml.R
import com.example.deliverycartusingxml.databinding.ActivityMapBinding
import com.example.deliverycartusingxml.geolocation.LocationFinder
import com.example.deliverycartusingxml.home.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var currentLocation: Location
    private var address: String? = null
    private val mapsViewModel: MapsViewModel by viewModel()
    private val locationFinder by lazy {
        LocationFinder(this)
    }

    private var binding: ActivityMapBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setUpUi()
        sendActivityResult()

    }

    private fun setUpUi() {
        fetchLocation()
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
        locationFinder.find { location ->
            currentLocation = location
            val supportMapFragment = (supportFragmentManager.findFragmentById(R.id.mapView) as
                    SupportMapFragment?)
            supportMapFragment?.getMapAsync(this)

            address = mapsViewModel.getAddress(
                currentLocation.latitude,
                currentLocation.longitude,
                this
            )
            binding?.uiTvUserAddress?.text = address
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val initialMarker: Marker?
        var currMarker: Marker? = null
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)

        initialMarker = googleMap?.addMarker(markerOptions)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

        googleMap?.setOnMapClickListener { lat_Lng ->
            address = mapsViewModel
                .getAddress(lat_Lng.latitude, lat_Lng.longitude, this@MapActivity)
            Log.d("userADDRESS", "onMapReady: $address")
            initialMarker?.remove()
            currMarker?.remove()
            currMarker = googleMap.addMarker(
                markerOptions
                    .position(lat_Lng ?: latLng)
            )
            binding?.uiTvUserAddress?.text = address
        }
    }

    private fun sendActivityResult() {
        binding?.uiBtnConfirm?.setOnClickListener {
            actionConfirm()
        }
    }

    private fun actionConfirm() {
        if (intent.getStringExtra(EXTRA_REQUEST_CODE) == "PICK_UP_KEY") {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(KEY_ADDRESS, address)
            setResult(
                100,
                intent
            )
            finish()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(KEY_ADDRESS, address)
            setResult(
                200,
                intent
            )
            finish()
        }
    }
}


