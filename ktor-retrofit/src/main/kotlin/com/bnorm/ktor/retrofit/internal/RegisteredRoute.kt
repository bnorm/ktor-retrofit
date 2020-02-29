package com.bnorm.ktor.retrofit.internal

import io.ktor.http.HttpMethod

internal data class RegisteredRoute private constructor(
    val method: HttpMethod,
    val baseUrl: String?,
    val route: String
) {
    // not included in constructor for equals/hashcode
    private var methodName: String = ""

    constructor(
        method: HttpMethod,
        baseUrl: String?,
        route: String,
        methodName: String
    ) : this(method, baseUrl, route) {
        this.methodName = methodName
    }
}
