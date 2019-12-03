package com.rubyhuntersky.data

sealed class ClipBase {
    data class Raw(val phrase: String) : ClipBase()
    data class Symbolic(val symbol: String, val phrase: String) : ClipBase()
}