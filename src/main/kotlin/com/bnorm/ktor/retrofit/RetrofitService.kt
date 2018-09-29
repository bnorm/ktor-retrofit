package com.bnorm.ktor.retrofit

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.features.conversionService
import io.ktor.http.HttpMethod
import io.ktor.pipeline.PipelineContext
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.head
import io.ktor.routing.options
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.AttributeKey
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.HTTP
import retrofit2.http.OPTIONS
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaType

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
          val serviceInterface = entry.key::class.superclasses.single { it != Any::class }
          route(entry.value ?: "/") {
            for (declaredFunction in serviceInterface.declaredFunctions) {
              process(entry.key, declaredFunction)
            }
          }
        }
      }
      return RetrofitService()
    }

    private fun Route.process(service: Any, function: KFunction<*>) {
      val annotations = function.annotations

      val get = annotations.filterIsInstance<GET>().singleOrNull()
      if (get != null) {
        get(get.value) {
          call.respond(invoke(call, service, function))
        }
        return
      }

      val post = annotations.filterIsInstance<POST>().singleOrNull()
      if (post != null) {
        post(post.value) {
          call.respond(invoke(call, service, function))
        }
        return
      }

      val delete = annotations.filterIsInstance<DELETE>().singleOrNull()
      if (delete != null) {
        delete(delete.value) {
          call.respond(invoke(call, service, function))
        }
        return
      }

      val put = annotations.filterIsInstance<PUT>().singleOrNull()
      if (put != null) {
        put(put.value) {
          call.respond(invoke(call, service, function))
        }
        return
      }

      val head = annotations.filterIsInstance<HEAD>().singleOrNull()
      if (head != null) {
        head(head.value) {
          call.respond(invoke(call, service, function))
        }
        return
      }

      val options = annotations.filterIsInstance<OPTIONS>().singleOrNull()
      if (options != null) {
        options(options.value) {
          call.respond(invoke(call, service, function))
        }
        return
      }

      val http = annotations.filterIsInstance<HTTP>().singleOrNull()
      if (http != null) {
        route(http.path, HttpMethod.parse(http.method)) {
          handle {
            call.respond(invoke(call, service, function))
          }
        }
        return
      }

      TODO("implement the rest of the function annotations? : $annotations")
    }

    private suspend fun PipelineContext<*, ApplicationCall>.invoke(
      call: ApplicationCall,
      service: Any,
      function: KFunction<*>
    ): Any {
      val conversionService = call.application.conversionService

      val parameters: Map<KParameter, Any?> = function.parameters.map {
        if (it.kind == KParameter.Kind.INSTANCE) {
          return@map it to service
        }

        val path = it.annotations.filterIsInstance<Path>().singleOrNull()
        if (path != null) {
          val values = call.parameters.getAll(path.value)
          if (values != null) {
            return@map it to conversionService.fromValues(values, it.type.javaType)
          } else {
            return@map it to null
          }
        }

        val query = it.annotations.filterIsInstance<Query>().singleOrNull()
        if (query != null) {
          val values = call.parameters.getAll(query.value)
          if (values != null) {
            return@map it to conversionService.fromValues(values, it.type.javaType)
          } else {
            return@map it to null
          }
        }

        val body = it.annotations.filterIsInstance<Body>().singleOrNull()
        if (body != null) {
          return@map it to call.receive(it.type.classifier as KClass<Any>)
        }

        TODO("annotations=${it.annotations}")
      }.toMap()

      // This is a big cheat
      // TODO(bnorm): is there a better way to pass through the action that needs to take place?
      val hasAction = function.callBy(parameters) as HasAction<Any>
      return hasAction.action(this)
    }
  }
}

typealias Action<T> = suspend PipelineContext<*, ApplicationCall>.() -> T

interface HasAction<T> {
  val action: Action<T>
}

fun <T> RetrofitService.Configuration.call(action: Action<T>): Call<T> {
  class KtorCall<T>(override val action: Action<T>) : Call<T>, HasAction<T> {
    override fun enqueue(callback: Callback<T>) = throw NotImplementedError("unused")
    override fun isExecuted() = throw NotImplementedError("unused")
    override fun clone() = throw NotImplementedError("unused")
    override fun isCanceled() = throw NotImplementedError("unused")
    override fun cancel() = throw NotImplementedError("unused")
    override fun execute() = throw NotImplementedError("unused")
    override fun request() = throw NotImplementedError("unused")
  }

  return KtorCall(action)
}
