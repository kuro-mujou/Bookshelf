package com.capstone.bookshelf.presentation.bookcontent.component.font

sealed interface FontAction {
    data class UpdateSelectedFontFamilyIndex(val index: Int) : FontAction
    data class UpdateFontSize(val fontSize: Int) : FontAction
    data class UpdateTextAlign(val textAlign: Boolean) : FontAction
    data class UpdateTextIndent(val textIndent: Boolean) : FontAction
    data class UpdateLineSpacing(val lineSpacing: Int) : FontAction
}