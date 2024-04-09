package com.example.booklette.model

import java.util.Locale

class SimpleFuzzySearch(private val arrayData: ArrayList<Map<String, String>>, private val attributes: ArrayList<String>, private var searchString: String? = null) {

    private var maxLD = 0.3
    private var minLCS = 0.7

    companion object {
        const val NOT_MATCH = 0
        const val STR2_STARTS_WITH_STR1 = 1
        const val STR2_CONTAINS_STR1 = 2
        const val STR1_STARTS_WITH_STR2 = 3
        const val STR1_CONTAINS_STR2 = 4
        const val LEVENSHTEIN_DISTANCE_CHECK = 5
        const val LONGEST_COMMON_SUBSTRING_CHECK = 6
    }

    fun getMaxLD(): Double {
        return maxLD
    }

    fun setMaxLD(ld: Double) {
        maxLD = ld
    }

    fun getMinLCS(): Double {
        return minLCS
    }

    fun setMinLCS(lcs: Double) {
        minLCS = lcs
    }

    fun search(searchString: String? = null): List<Array<Any>> {
        val results = mutableListOf<Array<Any>>()
        val search = searchString?.lowercase(Locale.ROOT) ?: this.searchString?.lowercase(Locale.ROOT)
        ?: return emptyList()
        if (search.isEmpty()) {
            return emptyList()
        }
        for (obj in arrayData) {
            var found = false
            for (attr in attributes) {
                if (found || !obj.containsKey(attr)) {
                    continue
                }
                val valStr = obj[attr]?.lowercase(Locale.ROOT) ?: continue
                if (valStr.isEmpty()) {
                    continue
                }
                var type = NOT_MATCH
                var typeVal = 0
                when {
                    search.contains(valStr) && search.indexOf(valStr) == 0 -> {
                        type = STR2_STARTS_WITH_STR1
                        typeVal = valStr.length
                    }
                    search.indexOf(valStr) > 0 -> {
                        type = STR2_CONTAINS_STR1
                        typeVal = valStr.length
                    }
                    valStr.contains(search) && valStr.indexOf(search) == 0 -> {
                        type = STR1_STARTS_WITH_STR2
                        typeVal = valStr.length
                    }
                    valStr.indexOf(search) > 0 -> {
                        type = STR1_CONTAINS_STR2
                        typeVal = valStr.length
                    }
                    checkLD(search, valStr) -> {
                        val ld = levenshtein(search, valStr)
                        type = LEVENSHTEIN_DISTANCE_CHECK
                        typeVal = ld / search.length
                    }
                    else -> {
                        val lcs = getLCS(valStr, search)
                        val similarPercent = lcs.length.toDouble() / search.length
                        if (similarPercent > minLCS) {
                            type = LONGEST_COMMON_SUBSTRING_CHECK
                            typeVal = lcs.length * (-1)
                        }
                    }
                }
                if (type != NOT_MATCH) {
                    results.add(arrayOf(obj, attr, type, typeVal))
                    found = true
                }
            }
        }
        results.sortWith(compareBy { it[2] as Int })
        return results
    }

    private fun checkLD(str1: String, str2: String): Boolean {
        val ld = levenshtein(str1, str2)
        return ld.toDouble() / str1.length <= maxLD
    }

    private fun levenshtein(str1: String, str2: String): Int {
        val lenStr1 = str1.length
        val lenStr2 = str2.length
        val matrix = Array(lenStr1 + 1) { IntArray(lenStr2 + 1) }
        for (i in 0..lenStr1) {
            matrix[i][0] = i
        }
        for (j in 0..lenStr2) {
            matrix[0][j] = j
        }
        for (i in 1..lenStr1) {
            for (j in 1..lenStr2) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                matrix[i][j] = minOf(matrix[i - 1][j] + 1, matrix[i][j - 1] + 1, matrix[i - 1][j - 1] + cost)
            }
        }
        return matrix[lenStr1][lenStr2]
    }

    private fun getLCS(str1: String, str2: String): String {
        val lenStr1 = str1.length
        val lenStr2 = str2.length
        val longestCommonSubstring = Array(lenStr1) { IntArray(lenStr2) }
        var largestSize = 0
        var returnStr = ""
        for (i in 0 until lenStr1) {
            for (j in 0 until lenStr2) {
                if (str1[i] == str2[j]) {
                    longestCommonSubstring[i][j] = if (i == 0 || j == 0) 1 else longestCommonSubstring[i - 1][j - 1] + 1
                    if (longestCommonSubstring[i][j] > largestSize) {
                        largestSize = longestCommonSubstring[i][j]
                        returnStr = ""
                    }
                    if (longestCommonSubstring[i][j] == largestSize) {
                        returnStr = str1.substring(i - largestSize + 1, i + 1)
                    }
                }
            }
        }
        return returnStr
    }
}
