package org.samvid.orchit.ui.fake

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.samvid.orchit.ui.ProjectListContract
import org.samvid.orchit.ui.ProjectListState
import org.samvid.orchit.ui.ProjectSummary

/** In-memory fake project list for UI development and previews. */
class FakeProjectListContract(
    initialProjects: List<ProjectSummary> = FakeProjectData.all(),
) : ProjectListContract {
    private val mutableState = MutableStateFlow(ProjectListState(initialProjects.toList()))
    private var nextId = initialProjects.size + 1

    override val state: StateFlow<ProjectListState> = mutableState

    override fun listProjects(): List<ProjectSummary> = mutableState.value.projects

    override fun createProject(name: String): ProjectSummary {
        val project = ProjectSummary(id = "proj-${nextId++.toString().padStart(3, '0')}", name = name)
        mutableState.value = mutableState.value.copy(projects = mutableState.value.projects + project)
        return project
    }

    override fun renameProject(projectId: String, name: String) {
        mutableState.value = mutableState.value.copy(
            projects = mutableState.value.projects.map { project ->
                if (project.id == projectId) project.copy(name = name) else project
            },
        )
    }

    override fun deleteProject(projectId: String) {
        val current = mutableState.value
        mutableState.value = current.copy(
            projects = current.projects.filterNot { it.id == projectId },
            openedProjectId = current.openedProjectId.takeUnless { it == projectId },
        )
    }

    override fun openProject(projectId: String): Boolean {
        if (mutableState.value.projects.none { it.id == projectId }) return false
        mutableState.value = mutableState.value.copy(openedProjectId = projectId)
        return true
    }
}
