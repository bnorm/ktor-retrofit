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

import com.bnorm.ktor.retrofit.internal.RegisteredRoute
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.features.conversionService
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondOutputStream
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.head
import io.ktor.routing.options
import io.ktor.routing.patch
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.AttributeKey
import okio.Okio
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.HTTP
import retrofit2.http.OPTIONS
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaType

private val registeredRoutes = mutableMapOf<HttpMethod, Set<RegisteredRoute>>()

fun Route.retrofitService(service: Any) {
  service::class.superclasses
    .filter { it != Any::class }
    .forEach { serviceInterface ->
      serviceInterface.declaredFunctions
        .forEach { declaredFunction ->
          if (!declaredFunction.isSuspend) TODO("only suspend Retrofit functions are supported")
          process(service, declaredFunction)
        }
    }
}

class RetrofitService {
  class Configuration {
    internal val services = mutableMapOf<Any, String?>()

    fun service(baseUrl: String? = null, service: Any) = services.put(service, baseUrl)
  }

  companion object Feature : ApplicationFeature<Application, Configuration, RetrofitService> {
    override val key = AttributeKey<RetrofitService>("Retrofit Service")

    override fun install(
      pipeline: Application,
      configure: Configuration.() -> Unit
    ): RetrofitService {
      val config = Configuration().apply(configure)
      pipeline.routing {
        for (entry in config.services) {
          route(entry.value ?: "/") {
            retrofitService(entry.key)
          }
        }
      }
      return RetrofitService()
    }
  }
}

private fun Route.process(service: Any, function: KFunction<*>) {
  val annotations = function.annotations

  val get = annotations.filterIsInstance<GET>().singleOrNull()
  if (get != null) {
    checkRegisteredRoute(HttpMethod.Get, get.value, function.name)
    get(get.value) {
      respond(call, service, function)
    }
    return
  }

  val post = annotations.filterIsInstance<POST>().singleOrNull()
  if (post != null) {
    checkRegisteredRoute(HttpMethod.Post, post.value, function.name)
    post(post.value) {
      respond(call, service, function)
    }
    return
  }

  val delete = annotations.filterIsInstance<DELETE>().singleOrNull()
  if (delete != null) {
    checkRegisteredRoute(HttpMethod.Delete, delete.value, function.name)
    delete(delete.value) {
      respond(call, service, function)
    }
    return
  }

  val put = annotations.filterIsInstance<PUT>().singleOrNull()
  if (put != null) {
    checkRegisteredRoute(HttpMethod.Put, put.value, function.name)
    put(put.value) {
      respond(call, service, function)
    }
    return
  }

  val patch = annotations.filterIsInstance<PATCH>().singleOrNull()
  if (patch != null) {
    checkRegisteredRoute(HttpMethod.Patch, patch.value, function.name)
    patch(patch.value) {
      respond(call, service, function)
    }
    return
  }

  val head = annotations.filterIsInstance<HEAD>().singleOrNull()
  if (head != null) {
    checkRegisteredRoute(HttpMethod.Head, head.value, function.name)
    head(head.value) {
      respond(call, service, function)
    }
    return
  }

  val options = annotations.filterIsInstance<OPTIONS>().singleOrNull()
  if (options != null) {
    checkRegisteredRoute(HttpMethod.Options, options.value, function.name)
    options(options.value) {
      respond(call, service, function)
    }
    return
  }

  val http = annotations.filterIsInstance<HTTP>().singleOrNull()
  if (http != null) {
    val method = HttpMethod.parse(http.method)
    checkRegisteredRoute(method, http.path, function.name)
    route(http.path, method) {
      handle {
        respond(call, service, function)
      }
    }
    return
  }

  TODO("implement the rest of the function annotations : $annotations")
}

private fun checkRegisteredRoute(method: HttpMethod, path: String, methodName: String) {
  val newRoute = RegisteredRoute(method, path, methodName)
  val registeredPaths = registeredRoutes.getOrDefault(method, mutableSetOf())
  if (registeredPaths.contains(newRoute)) {
    throw IllegalStateException("@${method.value}(\"$path\") is already registered")
  }
  registeredRoutes[method] = registeredPaths.toMutableSet().apply { add(newRoute) }
}

private suspend fun respond(call: ApplicationCall, service: Any, function: KFunction<*>) {
  val response = invoke(call, service, function)

  if (response is Response<*>) {
    call.response.status(HttpStatusCode.fromValue(response.code()))
    if (response.isSuccessful) {
      call.respond(response.body()!!)
    } else {
      call.respondOutputStream {
        response.errorBody()!!.use { error ->
          Okio.buffer(Okio.sink(this)).use { sink ->
            sink.writeAll(error.source())
          }
        }
      }
    }
  } else {
    call.respond(response)
  }
}

private suspend fun invoke(
  call: ApplicationCall,
  service: Any,
  function: KFunction<*>
): Any {
  val conversionService = call.application.conversionService

  val parameters: Array<Any?> = function.parameters.map {
    if (it.kind == KParameter.Kind.INSTANCE) {
      return@map service
    }

    val path = it.annotations.filterIsInstance<Path>().singleOrNull()
    if (path != null) {
      val values = call.parameters.getAll(path.value)
      if (values != null) {
        return@map conversionService.fromValues(values, it.type.javaType)
      } else {
        return@map null
      }
    }

    val query = it.annotations.filterIsInstance<Query>().singleOrNull()
    if (query != null) {
      val values = call.parameters.getAll(query.value)
      if (values != null) {
        return@map conversionService.fromValues(values, it.type.javaType)
      } else {
        return@map null
      }
    }

    val body = it.annotations.filterIsInstance<Body>().singleOrNull()
    if (body != null) {
      return@map call.receive(it.type.classifier as KClass<Any>)
    }

    TODO("annotations=${it.annotations}")
  }.toTypedArray()

  try {
    return function.callSuspend(*parameters) ?: TODO()
  } catch (t: InvocationTargetException) {
    throw t.targetException
  }
}
