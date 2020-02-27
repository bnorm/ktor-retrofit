package com.bnorm.ktor.retrofit

import retrofit2.http.GET
import retrofit2.http.Path

interface MultipleInterfaceService : MultipleInterfaceServiceOne, MultipleInterfaceServiceTwo

interface MultipleInterfaceServiceOne {
    @GET("string")
    suspend fun getString(): List<String>

    @GET("string/{id}")
    suspend fun getString(@Path("id") id: Long): String
}

interface MultipleInterfaceServiceTwo {
    @GET("int")
    suspend fun getInt(): List<Int>
}

class MultipleInterfaceServiceWrapper(
    multipleInterfaceServiceOne: MultipleInterfaceServiceOne,
    multipleInterfaceServiceTwo: MultipleInterfaceServiceTwo
) : MultipleInterfaceService,
    MultipleInterfaceServiceOne by multipleInterfaceServiceOne,
    MultipleInterfaceServiceTwo by multipleInterfaceServiceTwo

val multipleInterfaceService = MultipleInterfaceServiceWrapper(
    multipleInterfaceServiceOne = object : MultipleInterfaceServiceOne {
        override suspend fun getString(): List<String> {
            return listOf("one", "two")
        }

        override suspend fun getString(id: Long): String {
            return when (id) {
                0L -> "one"
                1L -> "two"
                else -> throw IndexOutOfBoundsException("id=$id")
            }
        }
    },
    multipleInterfaceServiceTwo = object : MultipleInterfaceServiceTwo {
        override suspend fun getInt(): List<Int> {
            return listOf(1, 2)
        }
    }
)