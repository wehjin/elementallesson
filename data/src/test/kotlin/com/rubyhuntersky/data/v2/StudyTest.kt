package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class StudyTest {

    @Test
    fun crud() {
        val tomic = tomic("crud", "studyTest")
        val owner = 1000L

        val created = tomic.createStudy("First Study", owner)
        assertEquals(owner, created.leader.ent)

        val read = tomic.latest.readStudies(owner)
        assertEquals("First Study", read.first()[Study.Name])

        val updated = tomic.updateStudy(read.first()) { Study.Name set "First Study - Renamed" }
        assertEquals("First Study - Renamed", updated.first()[Study.Name])

        val deleted = tomic.deleteStudy(updated.first().ent, owner)
        assertNotNull(deleted)
        assertEquals(0, tomic.latest.readStudies(owner).size)
    }
}