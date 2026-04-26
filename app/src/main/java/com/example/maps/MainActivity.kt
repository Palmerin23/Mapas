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
                    AppScreen()
                }
            }
        }
    }
}

@Composable
fun AppScreen() {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope() // Para llamadas a internet

    // Variables de estado
    var statusText by remember { mutableStateOf("Buscando señal GPS...") }
    var userLat by remember { mutableDoubleStateOf(0.0) }
    var userLon by remember { mutableDoubleStateOf(0.0) }

    var homeLat by remember { mutableDoubleStateOf(0.0) }
    var homeLon by remember { mutableDoubleStateOf(0.0) }

    var routePoints by remember { mutableStateOf<List<GeoPoint>?>(null) }

    // Tu Token de OpenRouteService
    val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImMyYzZhODIxMDZhYjRiZTFiY2E5MTY1MmI4YjMyZGFhIiwiaCI6Im11cm11cjY0In0=" // <--- CAMBIA ESTO POR TU KEY REAL

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (isGranted) {
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

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = statusText, color = MaterialTheme.colorScheme.primary)

        // EL MAPA
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
            val userGeoPoint = if (userLat != 0.0) GeoPoint(userLat, userLon) else null
            val homeGeoPoint = if (homeLat != 0.0) GeoPoint(homeLat, homeLon) else null

            OsmMap(
                userLocation = userGeoPoint,
                homeLocation = homeGeoPoint,
                routePoints = routePoints
            )
        }

        // BOTÓN 1: Guardar la ubicación actual como "Casa"
        Button(
            onClick = {
                if (userLat != 0.0) {
                    homeLat = userLat
                    homeLon = userLon
                    statusText = "¡Casa guardada exitosamente!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Establecer punto actual como 'Casa'")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BOTÓN 2: Calcular la ruta usando ORS
        Button(
            onClick = {
                if (userLat == 0.0 || homeLat == 0.0) {
                    statusText = "Falta ubicación o no has guardado tu casa"
                    return@Button
                }

                statusText = "Calculando ruta..."
                coroutineScope.launch {
                    try {
                        // ORS requiere que el string sea "Longitud,Latitud"
                        val startParam = "$userLon,$userLat"
                        val endParam = "$homeLon,$homeLat"

                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.getRoute(apiKey, startParam, endParam)
                        }

                        if (response.isSuccessful && response.body() != null) {
                            // Extraer las coordenadas del JSON de ORS
                            val coordinates = response.body()!!.features.firstOrNull()?.geometry?.coordinates

                            // Convertir la lista de ORS [Lon, Lat] a GeoPoints de OSMDroid (Lat, Lon)
                            if (coordinates != null) {
                                routePoints = coordinates.map { GeoPoint(it[1], it[0]) }
                                statusText = "¡Ruta trazada!"
                            }
                        } else {
                            statusText = "Error de API: ${response.code()}"
                        }
                    } catch (e: Exception) {
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