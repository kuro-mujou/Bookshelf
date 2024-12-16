package com.capstone.bookshelf.presentation.main

import com.capstone.bookshelf.domain.wrapper.Book

interface RootAction{
    data class OnTabSelected(val index: Int): RootAction
    data class OnBookClick(val book: Book): RootAction
    data class OnViewBookDetailClick(val book: Book): RootAction

}