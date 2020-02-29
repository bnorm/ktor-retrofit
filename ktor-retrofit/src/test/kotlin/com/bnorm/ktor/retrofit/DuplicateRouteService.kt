package com.bnorm.ktor.retrofit

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DuplicateRouteService {

    @GET("route")
    suspend fun getRoute(): String

    @GET("route/{id}")
    suspend fun getRoute(@Path("id") id: Long): String

    @POST("route")
    suspend fun postRoute(): String

    @GET("route")
    suspend fun getRouteAgain(): String
}

val duplicateRouteService = object : DuplicateRouteService {
    override suspend fun getRoute(): String = ""

    override suspend fun getRoute(id: Long): String = ""

    override suspend fun postRoute(): String = ""

    override suspend fun getRouteAgain(): String = ""
}