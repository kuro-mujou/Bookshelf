package com.capstone.bookshelf.util

fun cleanHtmlForJsoup(rawHtml: String): String {
    var html = rawHtml
    val tagsToFix = listOf("script", "style", "iframe", "noscript", "title")
    for (tag in tagsToFix) {
        html = html.replace(
            Regex("<$tag([^>]*)/>", RegexOption.IGNORE_CASE),
            "<$tag$1></$tag>"
        )
    }
    return html
}
