package com.capstone.bookshelf.presentation.bookwriter

import android.annotation.SuppressLint
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph

@SuppressLint("SdCardPath")
data class BookWriterState(
    val contentList: List<Paragraph> = emptyList(),
    val selectedItem: String = "",
    val itemToFocusId: String = "",
    val triggerScroll: Boolean = false,
    val linkPattern: Regex = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*"""),
    val headerPattern: Regex = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>"""),
    val headerLevel: Regex = Regex("""<h([1-6])>.*?</h\1>"""),
    val htmlTagPattern: Regex = Regex(pattern = """<[^>]+>"""),
    val triggerLoadChapter: Boolean = false
)
