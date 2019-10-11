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

package com.bnorm.hello

import com.bnorm.ktor.retrofit.RetrofitService
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.UUID

interface Service {
  @GET("string")
  suspend fun getAll(): List<String>

  @GET("string/{id}")
  suspend fun getSingle(@Path("id") id: Long): String
}

fun main() {
  embeddedServer(Netty, port = 8080) {
    install(CallLogging) {
      level = Level.INFO
      mdc("id") { UUID.randomUUID().toString().substring(0, 8) }
    }
    install(ContentNegotiation) {
      jackson { }
    }

    install(RetrofitService) {
      service(baseUrl = "api", service = object : Service {
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
      })
    }
  }.start(wait = true)
}
