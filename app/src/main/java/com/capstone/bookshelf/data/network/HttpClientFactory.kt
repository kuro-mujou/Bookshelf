package com.capstone.bookshelf.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            followRedirects = true

            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }
            install(HttpTimeout) {
                val longTimeoutMillis = 3 * 60 * 1000L
                socketTimeoutMillis = longTimeoutMillis
                requestTimeoutMillis = longTimeoutMillis
                connectTimeoutMillis = 30_000L
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.v("KtorHttpClient", message)
                    }
                }
                level = LogLevel.ALL
            }
            defaultRequest {
                headers.append(HttpHeaders.UserAgent, "Bookshelf/1.0 Android")
            }
        }
    }
}