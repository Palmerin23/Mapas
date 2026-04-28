package com.example.maps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices


// esta clse sirve como nuestro asistente de gps, recibe un context:
// que le indica en que parte de la app estamos trabajando para que tenga los permisos para usar esas funciones del sistema
class LocationHelper(context: Context) {

    // fusedLocationClient es la herramienta oficial de Google para rastrear la ubicacion

    //usamos fused porque es mas rapido y da lass cordenadas mas rapido y ahorranndo bateria
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


    //apaga la alerta de android, ya que piensa que usamos el gps sin pedir permiso, pero con esto le decimos que ya hicimos la verificacion de permismos
    @SuppressLint("MissingPermission")


    //funcio para saber donde estamos, onLocationReceived busca las cordenadas y las regresa cuando las tenga
    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {

        // Le pedimos al sistema la última ubicación conocida del dispositivo
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Si la búsqueda fue un éxito, ejecutamos nuestro recado devolviendo la latitud y longitud.
                onLocationReceived(location)
            }
            .addOnFailureListener {
                // Si algo falló ,devolvemos "null" para que la app sepa que falló y no se cierre de golpe
                onLocationReceived(null)
            }
    }
}