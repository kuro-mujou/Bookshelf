package com.capstone.bookshelf.data.network

import com.capstone.bookshelf.domain.error.DataError
import com.capstone.bookshelf.domain.error.MyResult
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.coroutineContext

suspend fun safeDownloadStream(
    httpClient: HttpClient,
    url: String
): MyResult<InputStream, DataError.Remote> {
    try {
        val response: HttpResponse = httpClient.get(url)
        if (!response.status.isSuccess()) {
            return MyResult.Error(mapHttpStatusToDataError(response.status))
        }
        val contentType = response.contentType()?.withoutParameters()
        if (contentType == ContentType.Text.Html) {
            return MyResult.Error(DataError.Remote.UNEXPECTED_CONTENT_TYPE_HTML)
        }
        val channel: ByteReadChannel = response.bodyAsChannel()
        val inputStream = withContext(Dispatchers.IO) {
            channel.toInputStream()
        }
        return MyResult.Success(inputStream)

    } catch (e: SocketTimeoutException) {
        coroutineContext.ensureActive()
        return MyResult.Error(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: UnresolvedAddressException) {
        coroutineContext.ensureActive()
        return MyResult.Error(DataError.Remote.NO_INTERNET)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        return MyResult.Error(DataError.Remote.UNKNOWN)
    }
}

fun mapHttpStatusToDataError(status: HttpStatusCode): DataError.Remote {
    return when (status.value) {
        408 -> DataError.Remote.REQUEST_TIMEOUT
        429 -> DataError.Remote.TOO_MANY_REQUESTS
        in 500..599 -> DataError.Remote.SERVER
        401, 403 -> DataError.Remote.UNAUTHORIZED
        404 -> DataError.Remote.NOT_FOUND
        else -> DataError.Remote.UNKNOWN
    }
}