package com.capstone.bookshelf.util

/**
 * Comparator for natural sorting of strings containing numbers.
 */
class NaturalOrderComparator : Comparator<String> {
    override fun compare(s1: String?, s2: String?): Int {
        if (s1 == null && s2 == null) return 0
        if (s1 == null) return -1
        if (s2 == null) return 1
        val num1 = extractNumber(s1)
        val num2 = extractNumber(s2)
        val numCompare = when {
            num1 != null && num2 != null -> num1.compareTo(num2)
            num1 != null -> 1
            num2 != null -> -1
            else -> 0
        }
        if (numCompare != 0) {
            return numCompare
        }
        return s1.compareTo(s2)
    }

    private fun extractNumber(s: String): Int? {
        val regex = "\\d+".toRegex()
        val match = regex.find(s)
        return match?.value?.toIntOrNull()
    }
}