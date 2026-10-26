package org.samvid.orchit.ui.fake

/**
 * Lightweight project summary for the home screen list.
 * This is a UI-only type, independent of the engine's Project class.
 */
data class ProjectSummary(
    val id: String,
    val name: String,
)

/**
 * Provides in-memory sample projects for UI development.
 */
object FakeProjectData {

    private val projects = mutableListOf(
        ProjectSummary(id = "proj-001", name = "My First Reel"),
        ProjectSummary(id = "proj-002", name = "Summer Highlights"),
    )

    fun all(): List<ProjectSummary> = projects.toList()

    fun add(project: ProjectSummary) {
        projects.add(project)
    }
}
