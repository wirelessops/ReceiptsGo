import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("com.github.triplet.play") version "3.8.1"
    id("org.jlleitschuh.gradle.ktlint")
    id("com.google.devtools.ksp")
    //id("com.google.android.gms.oss-licenses-plugin")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
localProperties.load(FileInputStream(localPropertiesFile))

android {
    namespace = "com.wops.receiptsgo"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1017
        versionName = "1.1.2"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true

        // Required for espresso (plus clears the app on launch)
        //testInstrumentationRunner("com.wops.receiptsgo.test.runner.ApplicationAwareAndroidJUnitRunner") = testInstrumentationRunner("com.wops.receiptsgo.test.runner.ApplicationAwareAndroidJUnitRunner")
        //testInstrumentationRunnerArguments(clearPackageData: = "com.wops.receiptsgo.test.runner.CrashingRunListener")
    }
    signingConfigs {

        register("debug") {
            storeFile = file("../keystore/debug.keystore")
        }
        register("release") {

            keyAlias = localProperties["keyAlias"] as String
            keyPassword = localProperties["keyPassword"] as String
            storeFile = file(localProperties["storeFile"] as String)
            storePassword = localProperties["storePassword"] as String

        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isShrinkResources = false
            isTestCoverageEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            //applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    bundle {
        language {
            // We include all languages to allow users to choose their report language
            enableSplit = false
        }
        density {
            // Performing initial tests with the split disabled for simplicity
            enableSplit = false
        }
        abi {
            // Force this reduce the size of PDF generation libraries to avoid an Android bug
            enableSplit = true
        }
    }

    packaging.resources.excludes += setOf(
        "META-INF/**",
        "kotlin/**",
        "**.bin",
        "**.properties"
    )

    flavorDimensions("versionType")

    productFlavors {
        create("free") {
            applicationId = "com.wops.receiptsgo"
            dimension = "versionType"
            proguardFile("flavor-not-floss-rules.pro")
            missingDimensionStrategy("isFloss", "notFloss")
        }

        create("plusFlavor") {
            applicationId = "com.wops.receiptsgopro"
            dimension = "versionType"
            proguardFile("flavor-not-floss-rules.pro")
            missingDimensionStrategy("isFloss", "notFloss")
        }

        create("flossFlavor") {
            applicationId = "com.wops.receiptsgo.floss"
            dimension = "versionType"
            missingDimensionStrategy("isFloss", "floss")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
}

//    testOptions {
//        animationsDisabled = true
//        unitTests {
//            returnDefaultValues = true
//            includeAndroidResources = true
//            all {
//                jvmArgs = "-noverify"
//            }
//        }
//        execution = "ANDROIDX_TEST_ORCHESTRATOR"
//    }

dependencies {

    implementation(fileTree(dir = "libs", include = "*.jar"))
    implementation(project(":core"))
    implementation(project(":wbMiniLibrary"))
    implementation(project(":aws"))
    implementation(project(":push"))
    implementation(project(":automatic_backups"))
    implementation(project(":oss_licenses"))
    implementation(platform(kotlin("bom", version = "1.8.0)"))


            implementation (libs.androidx.core.ktx)
            implementation (libs.androidx.appcompat)
            implementation (libs.androidx.cardview)
            implementation (libs.androidx.constraintlayout)
            implementation (libs.androidx.exifinterface)
            implementation (libs.androidx.legacy.support.v4)
            implementation (libs.androidx.multidex)
            implementation (libs.androidx.recyclerview)
            implementation (libs.com.google.android.material)
            implementation (libs.com.android.billingclient.billing)

//                implementation("com.google.apis:google-api-services-drive:$GOOGLE_DRIVE_API_VERSION") {
//            exclude(group = "org.apache.httpcomponents")
//        }
            implementation (libs.com.google.apis.api.services.drive)
            implementation (libs.com.google.http.client.gson)
            implementation (libs.com.squareup.picasso)
            implementation (libs.com.squareup.okhttp3.okhttp)
            implementation (libs.com.squareup.okhttp3.logging.interceptor)
            implementation (libs.com.squareup.retrofit2.retrofit)
            implementation (libs.com.squareup.retrofit2.converter.gson)
            implementation (libs.com.squareup.moshi)
            implementation (libs.com.squareup.moshi.adapters)
            //implementation(libs.com.squareup.moshi.kotlin.codegen)
            implementation (libs.com.squareup.retrofit2.converter.moshi)

            ksp (libs.com.squareup.moshi.kotlin.codegen)


            //Rx2
            implementation (libs.com.jakewharton.rxbinding3.rxbinding)
            implementation (libs.com.jakewharton.rxbinding3.rxbinding.appcompat)
            implementation (libs.com.squareup.retrofit2.adapter.rxjava2)
            implementation (libs.commons.io)
            implementation (libs.com.hadisatrio.Optional)
            implementation (libs.com.github.tapadoo.alerter)
            implementation (libs.com.tom.roush.pdfbox.android)
            implementation (libs.com.github.barteksc.pdfium.android)
            implementation (libs.com.github.tony19.logback.android)

            // Note: Periodically check for updates here so we can revert to the official version
            // implementation "com.github.PhilJay:MPAndroidChart:v3.1.0"

            implementation ("com.github.wbaumann:MPAndroidChart:v3.0.3.3")

            api ("com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0@aar")

            implementation (libs.com.hannesdorfmann.adapterdelegates4.kotlin.dsl.viewbinding)
            implementation (libs.com.github.pqpo.SmartCropper)
            implementation (libs.org.joda.money)
            implementation (libs.com.google.android.play.review.ktx)

            // Dagger
            kapt (libs.com.google.dagger.compiler)
            kapt (libs.com.google.dagger.android.processor)

            // Unit Tests
            testImplementation (libs.org.robolectric)
            testImplementation (libs.junit)
            testImplementation (libs.org.mockito.core)
            testImplementation (libs.com.nhaarman.mockitokotlin2.mockito.kotlin)
            testImplementation (libs.androidx.test.core)

            // Espresso Tests
            androidTestImplementation (libs.androidx.test.espresso.core)
            androidTestImplementation (libs.androidx.test.espresso.intents)
            androidTestImplementation (libs.androidx.test.ext.junit)
            androidTestImplementation (libs.androidx.test.runner)
            androidTestImplementation (libs.androidx.test.rules)

            //androidTestUtil(libs.androidx.test.orchestrator)

            // Leak Canary
            debugImplementation (libs.com.squareup.leakcanary.android)

            // Special dependencies for free flavor
            freeImplementation (libs.com.google.android.gms.play.services.analytics)

            // Free ads
            freeImplementation (libs.com.google.android.gms.play.services.ads)
}


ktlint {
    android.set(true)
}

//    lint {
//        abortOnError = false
//        disable("InvalidPackage", "MissingTranslation")
//    }