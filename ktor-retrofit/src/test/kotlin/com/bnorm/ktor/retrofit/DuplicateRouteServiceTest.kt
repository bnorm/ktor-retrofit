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
        exceptionRule.expectMessage(EXPECTED_ERROR_MESSAGE)
        withTestApplication(installFeature(duplicateRouteService)) {
        }
    }

    @Test
    fun route() {
        exceptionRule.expect(IllegalStateException::class.java)
        exceptionRule.expectMessage(EXPECTED_ERROR_MESSAGE)
        withTestApplication(installRoute(duplicateRouteService)) {
        }
    }
}

private const val EXPECTED_ERROR_MESSAGE = "@GET(\"route\") is already registered in com.bnorm.ktor.retrofit.DuplicateRouteService::getRoute"