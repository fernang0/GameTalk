package com.example.gametalk.model.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // IMPORTANTE: Cambia esta IP por la de tu servidor Spring Boot
    // Para emulador Android: usa 10.0.2.2 (localhost de tu PC)
    // Para dispositivo físico: usa la IP de tu PC en la red local
    private const val BASE_URL = "http://10.0.2.2:8080/"
    
    // Puedes cambiar la BASE_URL dinámicamente con este método
    fun setBaseUrl(url: String): ApiService {
        return createRetrofit(url).create(ApiService::class.java)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit: Retrofit by lazy {
        createRetrofit(BASE_URL)
    }
    
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
