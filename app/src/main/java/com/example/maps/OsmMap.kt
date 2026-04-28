package com.example.maps

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMap(
    modifier: Modifier = Modifier,
    userLocation: GeoPoint?,
    homeLocation: GeoPoint?,
    routePoints: List<GeoPoint>?,
    onMapLongPress: (GeoPoint) -> Unit
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),

        //factoty construte el mapa
        factory = { context ->
            // Requisito de OSMDroid para poder descargar los mapas de internet sin restricciones
            Configuration.getInstance().userAgentValue = context.packageName

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK) // Estilo visual de las calles
                setMultiTouchControls(true) // abilita el zoom con los dedos en la pantallaaaaaa
                controller.setZoom(16.0) // zoom inicial
            }
        },

        // se ejecuta ada que cambiamos de cordenadas
        update = { mapView ->
            // limpia el mapa antes de dibujar otra cosa
            mapView.overlays.clear()

            // detecta toques sobre el mapa
            val mapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean = false
                override fun longPressHelper(p: GeoPoint): Boolean {
                    // si deja el dedo presionadoa usaso esa como nueva cordenada
                    onMapLongPress(p)
                    return true
                }
            }
            mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))

            // pin de ubuicacion actual
            userLocation?.let {
                val userMarker = Marker(mapView).apply {
                    position = it
                    title = "Mi Ubicación Actual"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Anclar la punta del pin
                }
                mapView.overlays.add(userMarker)
                // si no hay ruta dibujada se entar en el ususario
                if (routePoints == null) mapView.controller.setCenter(it)
            }
    //pin de casaa
            homeLocation?.let {
                val homeMarker = Marker(mapView).apply {
                    position = it
                    title = "Mi Casa"
                    icon = mapView.context.getDrawable(android.R.drawable.ic_menu_compass) // Le ponemos un iconooo distinto
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(homeMarker)
            }

            // dibujacion de la ruta
            routePoints?.let { points ->
                val polyline = Polyline().apply {
                    setPoints(points) // Le pasamos las cordenadas
                    outlinePaint.color = android.graphics.Color.BLUE //linea del camino
                    outlinePaint.strokeWidth = 12f // ancho de la linea azul
                }
                mapView.overlays.add(polyline)
            }

            // refresca el mapa de inmediato para ver los cambios
            mapView.invalidate()
        }
    )
}