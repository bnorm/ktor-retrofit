# ktor-retrofit
Turns a Retrofit service interface into Ktor routing. Very early stage prototype.

You use Retrofit on the client side to access the end points of your server,
why not use that same interface to define the routings? The idea is to use an
implementation of the interface and install it as a feature into the Ktor,
dynamically construct routings from the annotations, and call the implementation
when the end point is accessed.

## Example

```kotlin
interface Service {
  @GET("string")
  fun getAll(): Call<List<String>>

  @GET("string/{id}")
  fun getSingle(@Path("id") id: Long): Call<String>
}

fun Application.module() {
  install(ContentNegotiation) {
    jackson { }
  }

  install(RetrofitService) {
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
}
```
