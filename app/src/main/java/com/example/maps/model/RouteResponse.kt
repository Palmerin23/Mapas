package com.example.maps.model

import com.google.gson.annotations.SerializedName

// Esta es la estructura que devuelve OpenRouteService
data class RouteResponse(
    @SerializedName("features") val features: List<Feature>
)

data class Feature(
    @SerializedName("geometry") val geometry: Geometry
)

data class Geometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>
    // ORS devuelve una lista de puntos [Longitud, Latitud]
)