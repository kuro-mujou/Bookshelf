package com.capstone.bookshelf.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource


sealed interface UiText {
    data class DynamicString(val value: String): UiText
    class StringResourceId(
        val id: Int
    ): UiText

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is StringResourceId -> stringResource(id = id)
        }
    }
}