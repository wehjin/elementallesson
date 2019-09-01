package com.rubyhuntersky.data.material.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class ListenLessonMaterialTest {

    @Test
    internal fun idDiffersWithDifferentLevels() {
        val a = ListenLessonMaterial(
            level = 1,
            foreignMediaPrompt = "fee",
            nativeTextResponse = "fie",
            foreignTextResponse = "foe"
        )
        val b = ListenLessonMaterial(
            level = 2,
            foreignMediaPrompt = "fee",
            nativeTextResponse = "fie",
            foreignTextResponse = "foe"
        )
        assertNotEquals(a.id, b.id)
    }

    @Test
    internal fun idDiffersWithDifferentPrompts() {
        val a = ListenLessonMaterial(
            level = 0,
            foreignMediaPrompt = "fee",
            nativeTextResponse = "fie",
            foreignTextResponse = "foe"
        )
        val b = ListenLessonMaterial(
            level = 0,
            foreignMediaPrompt = "fum",
            nativeTextResponse = "fie",
            foreignTextResponse = "foe"
        )
        assertNotEquals(a.id, b.id)
    }

    @Test
    internal fun idSameWithDifferentNativeResponses() {
        val a = ListenLessonMaterial(
            level = 0,
            foreignMediaPrompt = "fee",
            nativeTextResponse = "fie",
            foreignTextResponse = "foe"
        )
        val b = ListenLessonMaterial(
            level = 0,
            foreignMediaPrompt = "fee",
            nativeTextResponse = "fum",
            foreignTextResponse = "foe"
        )
        assertEquals(a.id, b.id)
    }

    @Test
    internal fun idSameWithDifferentForeignResponses() {
        val a = ListenLessonMaterial(
            level = 0,
            foreignMediaPrompt = "fee",
            nativeTextResponse = "fie",
            foreignTextResponse = "foe"
        )
        val b = ListenLessonMaterial(
            level = 0,
            foreignMediaPrompt = "fee",
            nativeTextResponse = "fie",
            foreignTextResponse = "fum"
        )
        assertEquals(a.id, b.id)
    }
}