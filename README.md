# ktor-retrofit

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bnorm.ktor.retrofit/ktor-retrofit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.bnorm.ktor.retrofit/ktor-retrofit)

Turns a Retrofit service interface into Ktor routing. Very early stage 
prototype.

You use Retrofit on the client side to access the end points of your server,
why not use that same interface to define the routings? The idea is to use an
implementation of the interface and add it to Ktor routing which will
dynamically construct sub-routings from the annotations and call the
implementation when the end point is accessed.

## Example

```kotlin
interface Service {
  @GET("string")
  suspend fun getAll(): List<String>

  @GET("string/{id}")
  suspend fun getSingle(@Path("id") id: Long): String
}

fun Application.module() {
  install(ContentNegotiation) {
    jackson { }
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
  
  routing { 
    route("api") {
      retrofitService(service = service)
    }
  }
}
```
