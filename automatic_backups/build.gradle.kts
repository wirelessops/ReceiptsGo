plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.wops.automatic_backups"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        //testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("isFloss", "floss")
    }


    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
    packaging.resources.excludes += setOf(
        "META-INF/**",
        "kotlin/**",
        "**.bin",
        "**.properties"
    )

    flavorDimensions += listOf("isFloss")
    productFlavors {
        create("floss") {
            dimension = "isFloss"
        }
        create("notFloss") {
            dimension = "isFloss"
        }
    }


}

configurations.forEach { it.exclude(group = "org.apache.httpcomponents") }

dependencies {
    implementation(project(":core"))
    implementation(project(":wbMiniLibrary"))

    implementation(libs.com.hadisatrio.optional)
    implementation(libs.com.google.apis.api.services.drive)

    "notFlossImplementation"(libs.play.services.auth)

    "notFlossImplementation"(libs.google.api.client.android)

    "notFlossImplementation"(libs.com.google.http.client.gson)

    // Unit Tests
    testImplementation(libs.org.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.org.mockito.core)
    testImplementation(libs.com.nhaarman.mockitokotlin2.mockito.kotlin)
    testImplementation(libs.androidx.test.core)
}
