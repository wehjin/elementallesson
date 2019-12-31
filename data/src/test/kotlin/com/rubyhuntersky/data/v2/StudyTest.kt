package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StudyTest {

    @Test
    fun crud() {
        val tomic = tomic("crud", "studyTest")
        val leader = 1000L

        val created = tomic.createStudy(leader, "First Study")
        assertEquals(leader, created.leader.ent)

        val read = tomic.readStudies(leader)
        assertEquals("First Study", read.first()[Study.Name])

        val updated = tomic.updateStudy(read.first()) { Study.Name set "First Study - Renamed" }
        assertEquals("First Study - Renamed", updated.first()[Study.Name])

        val deleted = tomic.deleteStudy(updated.first())
        assertEquals(0, deleted.size)
    }
}