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
import com.bnorm.ktor.retrofit.call
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*

interface Service {
  @GET("string")
  fun getAll(): Call<List<String>>

  @GET("string/{id}")
  fun getSingle(@Path("id") id: Long): Call<String>
}

object BackendService : Service {
  override fun getAll() = call {
    return@call listOf("first", "second")
  }

  override fun getSingle(id: Long) = call {
    return@call when (id) {
      0L -> "first"
      1L -> "second"
      else -> throw IndexOutOfBoundsException("id=$id")
    }
  }
}

fun main(args: Array<String>) {
  embeddedServer(Netty, port = 8080) {
    install(CallLogging) {
      level = Level.INFO
      mdc("id") { UUID.randomUUID().toString().substring(0, 8) }
    }
    install(StatusPages) {
      exception<Throwable> {
        call.respond(HttpStatusCode.InternalServerError)
      }
    }
    install(ContentNegotiation) {
      jackson { }
    }

    install(RetrofitService) {
      service(baseUrl = "api", service = BackendService)
    }
  }.start(wait = true)
}