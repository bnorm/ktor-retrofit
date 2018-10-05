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

import io.ktor.application.ApplicationCall
import io.ktor.pipeline.PipelineContext
import retrofit2.Call
import retrofit2.Callback

fun <T> call(block: PipelineContext<*, ApplicationCall>.() -> T): Call<T> {
  return object : Call<T>, ServiceAction<T> {
    override fun PipelineContext<*, ApplicationCall>.perform(): T = block()
    override fun enqueue(callback: Callback<T>) = throw NotImplementedError("unused")
    override fun isExecuted() = throw NotImplementedError("unused")
    override fun clone() = throw NotImplementedError("unused")
    override fun isCanceled() = throw NotImplementedError("unused")
    override fun cancel() = throw NotImplementedError("unused")
    override fun execute() = throw NotImplementedError("unused")
    override fun request() = throw NotImplementedError("unused")
  }
}
