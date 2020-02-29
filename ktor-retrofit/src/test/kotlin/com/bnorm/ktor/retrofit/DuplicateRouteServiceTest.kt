package com.bnorm.ktor.retrofit

import io.ktor.server.testing.withTestApplication
import org.hamcrest.core.StringContains
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException


class DuplicateRouteServiceTest {

    @Rule @JvmField
    val exceptionRule = ExpectedException.none()

    @Test
    fun feature() {
        exceptionRule.expect(IllegalStateException::class.java)
        exceptionRule.expectMessage("@GET(\"route\") is already registered")
        withTestApplication(installFeature(duplicateRouteService)) {
        }
    }

    @Test
    fun route() {
        exceptionRule.expect(IllegalStateException::class.java)
        exceptionRule.expectMessage("@GET(\"route\") is already registered")
        withTestApplication(installRoute(duplicateRouteService)) {
        }
    }
}