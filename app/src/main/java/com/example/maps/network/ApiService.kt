package com.example.maps.network

import com.example.maps.model.RouteResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


// con esta interfaz indicamos lo que nesesitamos pedirle a la api
interface ApiService {

    // es el pedacito de la dirección web que se encarga de calcular rutas para autos.
    @GET("v2/directions/driving-car")


    suspend fun getRoute(
        //arma el link de acceso de la appi
        @Query("api_key") apiKey: String, // Nuestra llave de acceso

        // encoded le dice a retrofit que respete las comillas para que no se rompa el link
        @Query("start", encoded = true) start: String,
        @Query("end", encoded = true) end: String
    ): Response<RouteResponse> // Le decimos que esperamos recibir la estructura de datos que hicimos
}



object RetrofitClient {

    // "by lazy" significa "perezoso".
    // espera hasyta el segundo esacto para trazar la ruta para no gastar energia
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            // La URL base o principal del servidor al que nos vamos a conectar
            .baseUrl("https://api.openrouteservice.org/")


            //agarra el jsons todoo feo y lo transforma a las variables, es un traductor

            .addConverterFactory(GsonConverterFactory.create())

            .build() // Ensamblamos el motor de internet
            .create(ApiService::class.java) // Le entregamos nuestro "Menú" (la interfaz de arriba) para que sepa qué rutas existen
    }
}