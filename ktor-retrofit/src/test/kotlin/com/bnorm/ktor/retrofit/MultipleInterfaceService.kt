/*
 * Copyright (C) 2018 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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