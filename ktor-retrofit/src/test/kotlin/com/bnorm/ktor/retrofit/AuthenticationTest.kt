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

import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.charset.Charset
import java.util.Base64

class AuthenticationTest {

  @Test
  fun route(): Unit = withTestApplication({
    install(ContentNegotiation) {
      jackson { }
    }

    install(Authentication) {
      basic {
        validate { credentials ->
          if (credentials.name == credentials.password) {
            UserIdPrincipal(credentials.name)
          } else {
            null
          }
        }
      }
    }

    routing {
      route("api") {
        authenticate {
          retrofitService(service = service)
        }
      }
    }
  }) {

    with(handleRequest(HttpMethod.Get, "/api/string") {
      addBasicAuthorization("someone", "someone")
    }) {
      assertEquals(HttpStatusCode.OK, response.status())
      assertEquals("[\"first\",\"second\"]", response.content)
    }

    with(handleRequest(HttpMethod.Get, "/api/string") {
      addBasicAuthorization("someone", "else")
    }) {
      assertEquals(HttpStatusCode.Unauthorized, response.status())
    }
  }

  private fun TestApplicationRequest.addBasicAuthorization(
    user: String,
    pass: String,
    charset: Charset = Charsets.ISO_8859_1
  ) {
    val auth = "$user:$pass".toByteArray(charset)
    val encoded = Base64.getEncoder().encode(auth).toString(charset = charset)
    addHeader(HttpHeaders.Authorization, "Basic $encoded")
  }
}
