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

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class ResponseTest {
  interface Service {
    @GET("string")
    suspend fun getAll(): Response<List<String>>

    @GET("string/{id}")
    suspend fun getSingle(@Path("id") id: Long): Response<String>
  }

  private val service = object : Service {
    override suspend fun getAll(): Response<List<String>> {
      return Response.success(listOf("first", "second"))
    }

    override suspend fun getSingle(id: Long): Response<String> {
      return when (id) {
        0L -> Response.success("first")
        1L -> Response.success("second")
        else -> Response.error(500, ResponseBody.create(null, "id=$id"))
      }
    }
  }

  @Test
  fun simpleRoute(): Unit = withTestApplication(installRoute(service)) {
    with(handleRequest(HttpMethod.Get, "/api/string")) {
      assertEquals(HttpStatusCode.OK, response.status())
      assertEquals("[\"first\",\"second\"]", response.content)
    }

    with(handleRequest(HttpMethod.Get, "/api/string/1")) {
      assertEquals(HttpStatusCode.OK, response.status())
      assertEquals("second", response.content)
    }
  }

  @Test
  fun simpleFeature(): Unit = withTestApplication(installFeature(service)) {
    with(handleRequest(HttpMethod.Get, "/api/string")) {
      assertEquals(HttpStatusCode.OK, response.status())
      assertEquals("[\"first\",\"second\"]", response.content)
    }

    with(handleRequest(HttpMethod.Get, "/api/string/1")) {
      assertEquals(HttpStatusCode.OK, response.status())
      assertEquals("second", response.content)
    }
  }

  @Test
  fun error(): Unit = withTestApplication(installRoute(service)) {
    with(handleRequest(HttpMethod.Get, "/api/string/2")) {
      assertEquals(HttpStatusCode.InternalServerError, response.status())
      assertEquals("id=2", response.content)
    }
  }
}
