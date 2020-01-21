package com.rubyhuntersky

import kotlin.math.min

fun <T> fillQuota(count: Int, split: Float, firstOptions: List<T>, secondOptions: List<T>): Pair<List<T>, List<T>> {
    val firstQuotaNominal = (split * count).toInt()
    val secondQuotaNominal = count - firstQuotaNominal
    val (firstQuota, secondQuota) = when {
        firstOptions.size < firstQuotaNominal ->
            Pair(firstOptions.size, min(secondOptions.size, count - firstOptions.size))
        secondOptions.size < secondQuotaNominal ->
            Pair(min(firstOptions.size, count - secondOptions.size), secondOptions.size)
        else -> {
            Pair(firstQuotaNominal, secondQuotaNominal)
        }
    }
    return Pair(firstOptions.take(firstQuota), secondOptions.take(secondQuota))
}
