plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
}
android {
    namespace = "com.wops.aws"
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

}

dependencies {

    implementation(project(":core"))

    implementation(libs.aws.android.sdk.core)
    implementation(libs.aws.android.sdk.s3)

    implementation(libs.com.hadisatrio.optional)

    // Unit Tests
    testImplementation(libs.org.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.org.mockito.core)
    testImplementation(libs.com.nhaarman.mockitokotlin2.mockito.kotlin)
    testImplementation(libs.androidx.test.core)

    // Dagger
    kapt(libs.com.google.dagger.compiler)
    kapt(libs.com.google.dagger.android.processor)

}
