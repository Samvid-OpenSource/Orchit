package org.samvid.orchit.ui.fake

import org.samvid.orchit.ui.ProjectSummary

/** Provides sample projects for the in-memory home screen contract. */
object FakeProjectData {
    /** Initial project rows shown in the UI skeleton. */
    fun all(): List<ProjectSummary> = listOf(
        ProjectSummary(id = "proj-001", name = "My First Reel"),
        ProjectSummary(id = "proj-002", name = "Summer Highlights"),
    )
}
