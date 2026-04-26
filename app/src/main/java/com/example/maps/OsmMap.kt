package com.example.maps

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMap(
    modifier: Modifier = Modifier,
    userLocation: GeoPoint?,
    homeLocation: GeoPoint?,
    routePoints: List<GeoPoint>?
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            // Requisito de OSMDroid para poder descargar los mapas de internet
            Configuration.getInstance().userAgentValue = context.packageName

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            // 1. Dibujar Marcador del Usuario
            userLocation?.let {
                val userMarker = Marker(mapView).apply {
                    position = it
                    title = "Mi Ubicación Actual"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(userMarker)
                mapView.controller.setCenter(it) // Centrar la cámara aquí
            }

            // 2. Dibujar Marcador de Casa
            homeLocation?.let {
                val homeMarker = Marker(mapView).apply {
                    position = it
                    title = "Mi Casa"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(homeMarker)
            }

            // 3. Dibujar la Ruta (Polyline)
            routePoints?.let { points ->
                val polyline = Polyline().apply {
                    setPoints(points)
                    outlinePaint.color = android.graphics.Color.RED // Línea roja
                    outlinePaint.strokeWidth = 10f
                }
                mapView.overlays.add(polyline)
            }

            mapView.invalidate() // Refrescar el mapa
        }
    )
}