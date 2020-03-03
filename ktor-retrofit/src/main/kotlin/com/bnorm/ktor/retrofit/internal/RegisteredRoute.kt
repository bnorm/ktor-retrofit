package com.bnorm.ktor.retrofit.internal

import io.ktor.http.HttpMethod

internal data class RegisteredRoute private constructor(
    val method: HttpMethod,
    val baseUrl: String?,
    val route: String
) {
    // not included in constructor for equals/hashcode
    var serviceInterface: String? = ""
        private set
    var methodName: String = ""
        private set

    constructor(
        method: HttpMethod,
        baseUrl: String?,
        route: String,
        serviceInterface: String?,
        methodName: String
    ) : this(method, baseUrl, route) {
        this.serviceInterface = serviceInterface
        this.methodName = methodName
    }
}
