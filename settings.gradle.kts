pluginManagement {

    repositories {
        google()

        maven(url = "https://maven.google.com")

        mavenCentral()

        maven(url = "https://jitpack.io")

        maven(url = "https://plugins.gradle.org/m2/")
    }
}

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven(url = "https://jitpack.io")

        maven(url = "https://plugins.gradle.org/m2/")
    }
}

include("app", "aws", "core", "push", "analytics", "automatic_backups", "oss_licenses")
include("wbMiniLibrary")

rootProject.name = "ReceiptsGo"