package com.capstone.bookshelf.presentation.bookwriter

import android.annotation.SuppressLint
import com.capstone.bookshelf.presentation.bookwriter.component.ParagraphType

@SuppressLint("SdCardPath")
data class BookWriterState(
    val addingState: Boolean = false,
    val addIndex: Int = -1,
    val addType: ParagraphType = ParagraphType.NONE,
    val selectedItem: Int = -1,
    val triggerScroll: Boolean = false,
    val linkPattern: Regex = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*"""),
    val headerPattern: Regex = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>"""),
    val headerLevel: Regex = Regex("""<h([1-6])>.*?</h\1>"""),
    val htmlTagPattern: Regex = Regex(pattern = """<[^>]+>"""),

    val toggleBold: Boolean = false,
    val toggleItalic: Boolean = false,
    val toggleUnderline: Boolean = false,
    val toggleStrikethrough: Boolean = false,
    val toggleAlign: Int = 1,
)
