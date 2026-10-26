pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Orchit"
include(":app", ":engine", ":effects", ":editor-ui", ":audio", ":text")
project(":engine").projectDir = file("modules/engine")
project(":effects").projectDir = file("modules/effects")
project(":editor-ui").projectDir = file("modules/ui")
project(":audio").projectDir = file("modules/audio")
project(":text").projectDir = file("modules/text")
