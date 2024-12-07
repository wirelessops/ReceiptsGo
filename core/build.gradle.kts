plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
}

android {
    namespace = "com.wops.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        //testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
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
    /**
     * Note: core module doesn't have flavor-dependent code but it needs flavors because of transitive analytics module
     */
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

dependencies {

    api(project(":analytics"))

    // Dagger
    kapt(libs.com.google.dagger.compiler)
    kapt(libs.com.google.dagger.android.processor)

    //Rx2
    api(libs.rxjava)
    api(libs.rxandroid)

    // Unit Tests
    testImplementation(libs.org.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.org.mockito.core)
    testImplementation(libs.com.nhaarman.mockitokotlin2.mockito.kotlin)
    testImplementation(libs.androidx.test.core)

    implementation(libs.com.google.http.client.gson)
}
