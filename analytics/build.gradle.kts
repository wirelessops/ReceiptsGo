plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
}

android {
    namespace = "com.wops.analytics"
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

    //implementation(platform(libs.firebase.bom))

    // Dagger
    api(libs.dagger.android.support)
    api(libs.dagger)

    kapt(libs.com.google.dagger.compiler)
    kapt(libs.com.google.dagger.android.processor)

    api(libs.guava)

    // Logger
    api(libs.slf4j.api)


    implementation(libs.firebase.analytics)

    "notFlossImplementation"(libs.firebase.crashlytics)
}
