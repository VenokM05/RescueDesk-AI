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

rootProject.name = "RescueDeskAI"
include(":app")
// Phase 1 model PoC spike — separate applicationId, never ships with :app
// (docs/PHASE1-MODEL-POC.md §5.1).
include(":poc")
