package com.example.deliverycartusingxml.geolocation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

@SuppressLint("MissingPermission")
class LocationFinder(private val activity: Activity) : LocationCallback() {
    private var finderType: FinderType = FinderType.GPS
    private var updateInterval: Long = 0
    private var fastestInterval: Long = 0
    private lateinit var locationUpdateListener: (Location) -> Unit
    private lateinit var locationRequest: LocationRequest
    private var gpsRequestListener: GpsRequestListener? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_GPS_REQ, 0)) {
                Activity.RESULT_OK -> {
                    LocationServices.getFusedLocationProviderClient(activity)
                        .requestLocationUpdates(locationRequest,
                            this@LocationFinder,
                            Looper.myLooper())
                    gpsRequestListener?.gpsTurnedOn()
                }
                Activity.RESULT_CANCELED -> {
                    gpsRequestListener?.gpsNotTurnedOn()
                }
            }
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = updateInterval
        locationRequest.fastestInterval = fastestInterval
    }

    fun find(finderType: FinderType = FinderType.GPS, listener: (Location) -> Unit) {
        this.finderType = finderType
        this.locationUpdateListener = listener
        createLocationRequest()
        enableGpsRequestCallback()
        find()
    }

    private fun find() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true) // this is the key ingredient

        val result =
            LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
        result.addOnCompleteListener { res ->
            try {
                val response = res.getResult(ApiException::class.java)
                    ?: return@addOnCompleteListener
                val states = response.locationSettingsStates
                when {
                    states?.isLocationUsable == true -> LocationServices.getFusedLocationProviderClient(
                        activity).requestLocationUpdates(
                        locationRequest,
                        this@LocationFinder,
                        Looper.myLooper()
                    )
                    states?.isGpsPresent == true -> {

                    }
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            (e as ResolvableApiException).startResolutionForResult(
                                this@LocationFinder.activity,
                                GPS_REQUEST
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        } catch (e: ClassCastException) {
                            e.printStackTrace()
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        Log.d("TAG", "Failed to turn on GPS")
                    }
                }
            }
        }
    }

    fun enableGpsRequestCallback(gpsRequestListener: GpsRequestListener? = null) {
        this.gpsRequestListener = gpsRequestListener
        LocalBroadcastManager.getInstance(activity)
            .registerReceiver(receiver, IntentFilter(BROADCAST_GPS_REQ))
    }

    fun config(updateInterval: Long, fastestInterval: Long) {
        this.updateInterval = updateInterval
        this.fastestInterval = fastestInterval
    }

    private fun stopTracking() {
        LocationServices.getFusedLocationProviderClient(activity)
            .removeLocationUpdates(this@LocationFinder)
    }

    fun stopFinder() {
        try {
            stopTracking()
            activity.unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationResult(locationResult: LocationResult) {
        locationResult.lastLocation?.let { onLocationChanged(it) }
    }

    private fun onLocationChanged(location: Location) {
        if (finderType != FinderType.TRACK)
            stopTracking()
        locationUpdateListener(location)
    }

    enum class FinderType {
        /**
         * It will update the current GPS location once.
         */
        GPS,

        /**
         * It will update the current Network location once.
         */
        NETWORK,

        /**
         * It will update the user location continuously.
         */
        TRACK
    }


    interface GpsRequestListener {
        fun gpsTurnedOn()

        fun gpsNotTurnedOn()
    }


    companion object {
        private const val BROADCAST_GPS_REQ = "LocationFinder.GPS_REQ"
        private const val KEY_GPS_REQ = "key.gps.req"
        private const val GPS_REQUEST = 2301

        fun onRequestResult(activity: Activity, requestCode: Int, resultCode: Int) {
            if (requestCode == GPS_REQUEST) {
                val intent = Intent(BROADCAST_GPS_REQ)
                intent.putExtra(KEY_GPS_REQ, resultCode)
                LocalBroadcastManager.getInstance(activity)
                    .sendBroadcast(intent)
            }
        }
    }
}