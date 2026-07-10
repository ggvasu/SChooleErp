package com.example.data.repository

import android.content.Context
import com.example.data.session.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST
    suspend fun executeAction(
        @Url url: String,
        @Body body: RequestBody
    ): Response<ResponseBody>
}

open class BaseRepository(
    protected val context: Context,
    protected val sessionManager: SessionManager
) {
    // MOSHI Setup
    protected val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // RETROFIT setup with automatic redirect handling
    protected val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    protected val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://script.google.com/") // Base URL is required, though we use dynamic full URLs
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
