/*
 * Copyright (C) 2020 Brian Norman
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

interface Service {
  @GET("string")
  suspend fun getAll(): List<String>

  @GET("string/{id}")
  suspend fun getSingle(@Path("id") id: Long): String
}

val service = object : Service {
  override suspend fun getAll(): List<String> {
    return listOf("first", "second")
  }

  override suspend fun getSingle(id: Long): String {
    return when (id) {
      0L -> "first"
      1L -> "second"
      else -> throw IndexOutOfBoundsException("id=$id")
    }
  }
}
