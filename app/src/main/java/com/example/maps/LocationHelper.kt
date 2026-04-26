package com.example.maps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices

class LocationHelper(context: Context) {
    // Cliente de servicios de ubicación de Google
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // La verificación se hace en la interfaz (Compose)
    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onLocationReceived(location)
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    }
}