package com.capstone.bookshelf.domain.error

sealed interface MyResult<out D, out E: Error> {
    data class Success<out D>(val data: D): MyResult<D, Nothing>
    data class Error<out E: com.capstone.bookshelf.domain.error.Error>(val error: E):
        MyResult<Nothing, E>
}

inline fun <T, E: Error, R> MyResult<T, E>.map(map: (T) -> R): MyResult<R, E> {
    return when(this) {
        is MyResult.Error -> MyResult.Error(error)
        is MyResult.Success -> MyResult.Success(map(data))
    }
}

fun <T, E: Error> MyResult<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

inline fun <T, E: Error> MyResult<T, E>.onSuccess(action: (T) -> Unit): MyResult<T, E> {
    return when(this) {
        is MyResult.Error -> this
        is MyResult.Success -> {
            action(data)
            this
        }
    }
}
inline fun <T, E: Error> MyResult<T, E>.onError(action: (E) -> Unit): MyResult<T, E> {
    return when(this) {
        is MyResult.Error -> {
            action(error)
            this
        }
        is MyResult.Success -> this
    }
}

typealias EmptyResult<E> = MyResult<Unit, E>