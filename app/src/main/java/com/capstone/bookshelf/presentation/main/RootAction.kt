package com.capstone.bookshelf.presentation.main

interface RootAction{
    data class OnTabSelected(val index: Int): RootAction
}