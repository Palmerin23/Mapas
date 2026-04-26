package com.example.maps.network

import com.example.maps.model.RouteResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // Usamos el perfil de manejo en auto (driving-car)
    @GET("v2/directions/driving-car")
    suspend fun getRoute(
        @Query("api_key") apiKey: String,
        @Query("start", encoded = true) start: String, // Formato: "longitud,latitud"
        @Query("end", encoded = true) end: String      // Formato: "longitud,latitud"
    ): Response<RouteResponse>
}

// Objeto para instanciar Retrofit fácilmente
object RetrofitClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}