plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
}

android {
    namespace = "com.wops.push"
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
    implementation(project(":core"))

    // Dagger
    kapt(libs.com.google.dagger.compiler)
    kapt(libs.com.google.dagger.android.processor)

    // firebase
    //notFlossImplementation(platform(libs.firebase.bom))
    "notFlossImplementation"(libs.firebase.crashlytics)
    // we must use api here during to this dagger issue https://github.com/google/dagger/issues/970 (stackoverflow https://stackoverflow.com/questions/47124987/dagger-2-cannot-access-retrofit)
    "notFlossApi"(libs.firebase.messaging)

   // notFlossApi("com.google.firebase:firebase-iid:21.1.0")

    // Unit Tests
    testImplementation(libs.org.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.org.mockito.core)
    testImplementation(libs.com.nhaarman.mockitokotlin2.mockito.kotlin)
    testImplementation(libs.androidx.test.core)
}
