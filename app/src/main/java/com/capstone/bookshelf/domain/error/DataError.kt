package com.capstone.bookshelf.domain.error

sealed interface DataError: Error {
    enum class Remote : DataError {
        REQUEST_TIMEOUT,
        NO_INTERNET,
        UNKNOWN,
        SERIALIZATION,
        TOO_MANY_REQUESTS,
        SERVER,
        UNAUTHORIZED,
        NOT_FOUND,
        UNEXPECTED_CONTENT_TYPE_HTML,
        HTML_PARSING_FAILED,
        DOWNLOAD_CONFIRMATION_FAILED
    }

    enum class Local: DataError {
        DISK_FULL,
        UNKNOWN
    }
}