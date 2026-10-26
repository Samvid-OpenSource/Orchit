package org.samvid.orchit.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProjectJsonTest {
    @Test
    fun sampleRoundTrips() {
        val source = javaClass.classLoader!!.getResourceAsStream("project.json")!!.bufferedReader().use { it.readText() }
        val decoded = ProjectJson.read(source)
        assertEquals(decoded, ProjectJson.read(ProjectJson.write(decoded)))
    }

    @Test
    fun rejectsUnknownFieldsAndUnsupportedVersion() {
        val source = javaClass.classLoader!!.getResourceAsStream("project.json")!!.bufferedReader().use { it.readText() }
        assertThrows(IllegalArgumentException::class.java) {
            ProjectJson.read(source.replace("\"formatVersion\": 1", "\"formatVersion\": 2"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProjectJson.read(source.replace("\"name\": \"Sample edit\"", "\"name\": \"Sample edit\", \"unknown\": true"))
        }
    }
}
