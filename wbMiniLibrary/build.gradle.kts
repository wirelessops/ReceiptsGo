plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
}

android {
    namespace = "wb.android"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        multiDexEnabled = true
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }

}

dependencies {
    api(libs.androidx.legacy.support.v4)

    // Dagger
    //api "com.google.dagger:dagger-android-support:$DAGGER_VERSION"
    api(libs.dagger)
    kapt(libs.com.google.dagger.compiler)
    kapt(libs.com.google.dagger.android.processor)
    //implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0"
    implementation(libs.androidx.exifinterface)

}