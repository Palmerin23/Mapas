package com.example.maps

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import com.example.maps.network.RetrofitClient
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.maps.ui.theme.MapsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen() // Llamamos a nuestra interfaz principal
                }
            }
        }
    }
}

// Composable principal que contiene toda la lógica y la interfaz visual
@Composable
fun AppScreen() {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }

    // nos deja hacer taraea pesadas sin que se trabe la pantalla, como busquedas en internet
    val coroutineScope = rememberCoroutineScope()

    // vasriables de estado para actualizar la pantalla automaticamente al cambiar
    var statusText by remember { mutableStateOf("Buscando señal GPS...") } // Mensaje en texto para el usuario
    var userLat by remember { mutableDoubleStateOf(0.0) } // Latitud del dispositivo
    var userLon by remember { mutableDoubleStateOf(0.0) } // Longitud del dispositivo

    var homeLat by remember { mutableDoubleStateOf(0.0) } // Latitud de la "Casa"
    var homeLon by remember { mutableDoubleStateOf(0.0) } // Longitud de la "Casa"

    // Guarda la lista de puntos de la ruta trazada, si es null, no dibuja nada
    var routePoints by remember { mutableStateOf<List<GeoPoint>?>(null) }

    // token de autenticacion para usar la appi de rutas de OpenRouteService
    val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImMyYzZhODIxMDZhYjRiZTFiY2E5MTY1MmI4YjMyZGFhIiwiaCI6Im11cm11cjY0In0="


    // Esta función lanza la ventanita preguntando al usuario si da permiso de usar el GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // Verificamos si nos dieron permiso de ubicación precisa
            val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (isGranted) {
                // Si hay permiso, buscamos las coordenadas actuales mediante LocationHelper
                locationHelper.getCurrentLocation { location ->
                    if (location != null) {
                        userLat = location.latitude
                        userLon = location.longitude
                        statusText = "Ubicación lista"
                    } else {
                        statusText = "No se pudo obtener el GPS"
                    }
                }
            } else {
                statusText = "Permiso denegado"
            }
        }
    )

    // pide el permiso automaticamente apenas se abra la pantalla
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }


    // Column ordena los elementos verticalmente
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        //informa el estado actual de la app
        Text(text = statusText, color = MaterialTheme.colorScheme.primary)

        // el mapa ocupa el espacio sobrante en la pantalla gracias al weight(1f)
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
            // Convertimos nuestras variables a "GeoPoints" (formato que usa OSMDroid) solo si ya tienen datos reales
            val userGeoPoint = if (userLat != 0.0) GeoPoint(userLat, userLon) else null
            val homeGeoPoint = if (homeLat != 0.0) GeoPoint(homeLat, homeLon) else null

            // Llamamos a nuestro componente de mapa personalizado
            OsmMap(
                userLocation = userGeoPoint,
                homeLocation = homeGeoPoint,
                routePoints = routePoints,
                onMapLongPress = { clickedPoint ->
                    // Cuando el usuario deja presionado un punto en el mapa, actualizamos las coordenadas de la "Casa"
                    homeLat = clickedPoint.latitude
                    homeLon = clickedPoint.longitude
                    statusText = "Ubicación de Casa actualizada en el mapa"
                }
            )
        }

        // boton 1 guarda la ubicacion actual como casa(en donde nos econtremos literalmente )
        Button(
            onClick = {
                if (userLat != 0.0) {
                    // Copiamos la posición actual a las variables de casa
                    homeLat = userLat
                    homeLon = userLon
                    statusText = "¡Casa guardada exitosamente!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Establecer punto actual como 'Casa'")
        }

        Spacer(modifier = Modifier.height(8.dp))//espacio entre botones

        // el boton dos se conecta a internet y calcula la ruta usando OpenRouteService
        Button(
            onClick = {
                // Validamos que tengamos ambas ubicaciones definidas antes de consultar la API
                if (userLat == 0.0 || homeLat == 0.0) {
                    statusText = "Falta ubicación o no has guardado tu casa"
                    return@Button
                }

                statusText = "Calculando ruta..."

                //usamos una corrutina para que la peticion de red no trane la interfaz visual
                coroutineScope.launch {
                    try {
                        // preparamos los datos que le vamos a mandar a ORS
                        val startParam = "$userLon,$userLat"
                        val endParam = "$homeLon,$homeLat"

                        // corremos la peticion http a la api
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.getRoute(apiKey, startParam, endParam)
                        }

                        // si nos responde el servidor con el codigo 200 es que todo esta bien y nos trae los datos
                        if (response.isSuccessful && response.body() != null) {

                            // Extraemos el arreglo de coordenadas geométricas del JSON de respuesta
                            val coordinates = response.body()!!.features.firstOrNull()?.geometry?.coordinates

                            // ORS nos devuelve las coordenadas en orden [Longitud, Latitud]
                            // OSMDroid necesita que las metamos como GeoPoint(Latitud, Longitud), por eso usamos .map para voltearlas
                            if (coordinates != null) {
                                routePoints = coordinates.map { GeoPoint(it[1], it[0]) }
                                statusText = "¡Ruta trazada!"
                            }
                        } else {
                            //errror desde el servidor
                            statusText = "Error de API: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        // Atrapamos errores locales como itenet o cualquier cosa
                        statusText = "Error de conexión: ${e.localizedMessage}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Trazar Ruta a Casa")
        }
    }
}