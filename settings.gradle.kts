pluginManagement {
    resolutionStrategy {
        eachPlugin {
            // Force resolution due to missing Plugin Marker Artifact with the coordinates plugin.id:plugin.id.gradle.plugin:plugin.version
            if (requested.id.id == "com.google.android.gms.oss-licenses-plugin") {
                useModule("com.google.android.gms:oss-licenses-plugin:0.10.6")
            }
        }
    }
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

rootProject.name = "receipts-go"