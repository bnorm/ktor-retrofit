package com.bnorm.ktor.retrofit.internal

import io.ktor.http.HttpMethod

data class RegisteredRoute private constructor(
    val method: HttpMethod,
    val route: String
) {
    // not included in constructor for equals/hashcode
    var methodName: String = ""
        private set

    constructor(
        method: HttpMethod,
        route: String,
        methodName: String
    ) : this(method, route) {
        this.methodName = methodName
    }
}
