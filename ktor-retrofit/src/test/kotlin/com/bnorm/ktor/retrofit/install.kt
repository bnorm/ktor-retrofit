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

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing

typealias ApplicationScope = Application.() -> Unit

fun installFeature(service: Any): ApplicationScope = {
  install(ContentNegotiation) {
    jackson { }
  }

  install(RetrofitService) {
    service(baseUrl = "api", service = service)
  }
}

fun installRoute(service: Any): ApplicationScope = {
  install(ContentNegotiation) {
    jackson { }
  }

  routing {
    route("api") {
      retrofitService(service = service)
    }
  }
}
