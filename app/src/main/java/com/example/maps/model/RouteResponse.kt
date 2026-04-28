package com.example.maps.model



data class RouteResponse(
    // La API nos manda los datos dentro de un arreglo llamado "features"
    val features: List<Feature>
)

data class Feature(
    //cordenadas de las calles trazadas
    val geometry: Geometry
)

// líneas geográficas de la ruta
data class Geometry(
    val coordinates: List<List<Double>>
)