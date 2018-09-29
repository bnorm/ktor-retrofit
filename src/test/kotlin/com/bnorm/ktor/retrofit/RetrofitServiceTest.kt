package com.bnorm.ktor.retrofit

import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface Service {
  @GET("string")
  fun getAll(): Call<List<String>>

  @GET("string/{id}")
  fun getSingle(@Path("id") id: Long): Call<String>
}

class RetrofitServiceTest {

  @Test
  fun feature(): Unit = withTestApplication {
    //    application.install(DataConversion)
    application.install(ContentNegotiation) {
      jackson { }
    }

    application.install(RetrofitService) {
      service(baseUrl = "api", service = object : Service {
        override fun getAll(): Call<List<String>> = call {
          return@call listOf("first", "second")
        }

        override fun getSingle(id: Long): Call<String> = call {
          return@call when (id) {
            0L -> "first"
            1L -> "second"
            else -> throw IndexOutOfBoundsException("id=$id")
          }
        }
      })
    }

    with(handleRequest(HttpMethod.Get, "/api/string")) {
      assertEquals(HttpStatusCode.OK, response.status())
      assertEquals("[\"first\",\"second\"]", response.content)
    }

    with(handleRequest(HttpMethod.Get, "/api/string/1")) {
      assertEquals(HttpStatusCode.OK, response.status())
      assertEquals("second", response.content)
    }

    // with(handleRequest(HttpMethod.Get, "/api/string/2")) {
    //   assertEquals(HttpStatusCode.InternalServerError, response.status())
    // }
  }
}
