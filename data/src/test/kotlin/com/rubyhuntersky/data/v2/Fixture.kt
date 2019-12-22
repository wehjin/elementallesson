package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.tomicOf

fun tomic(prefix: String, suffix: String): Tomic {
    val dir = createTempDir("$prefix-", ".$suffix")
    val tomic = tomicOf(dir) { emptyList() }
    return tomic.also { println("Location: $dir") }
}
