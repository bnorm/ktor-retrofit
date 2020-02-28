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

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

private fun TestApplicationEngine.runSimpleTest() {
  with(handleRequest(HttpMethod.Get, "/api/string")) {
    assertEquals(HttpStatusCode.OK, response.status())
    assertEquals("[\"first\",\"second\"]", response.content)
  }

  with(handleRequest(HttpMethod.Get, "/api/string/1")) {
    assertEquals(HttpStatusCode.OK, response.status())
    assertEquals("second", response.content)
  }
}

class RetrofitServiceTest {
  @Test
  fun feature(): Unit = withTestApplication(installFeature(service)) {
    runSimpleTest()
  }

  @Test
  fun route(): Unit = withTestApplication(installRoute(service)) {
    runSimpleTest()
  }

  @Test
  fun error(): Unit = withTestApplication({
    installFeature(service).invoke(this)
    install(StatusPages) {
      exception<Throwable> {
        call.respond(HttpStatusCode.InternalServerError, "Error")
      }
    }
  }) {
    with(handleRequest(HttpMethod.Get, "/api/string/2")) {
      assertEquals(HttpStatusCode.InternalServerError, response.status())
      assertEquals("Error", response.content)
    }
  }
}
