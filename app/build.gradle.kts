plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

val defaultVersionCode = 1
val defaultVersionName = (project.findProperty("appVersionName") as String?) ?: "1.0.0"
val resolvedVersionCode =
    (project.findProperty("ciVersionCode") as String?)?.toIntOrNull() ?: defaultVersionCode
val resolvedVersionName =
    (project.findProperty("ciVersionName") as String?) ?: defaultVersionName

fun resolveSigningValue(propertyName: String): String? {
    return (project.findProperty(propertyName) as String?)
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv(propertyName)?.takeIf { it.isNotBlank() }
}

val releaseStoreFile = resolveSigningValue("RELEASE_STORE_FILE")
val releaseStorePassword = resolveSigningValue("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = resolveSigningValue("RELEASE_KEY_ALIAS")
val releaseKeyPassword = resolveSigningValue("RELEASE_KEY_PASSWORD")

android {
    namespace = "com.cirin0.worktimetracker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.cirin0.worktimetracker"
        minSdk = 31
        targetSdk = 36
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (
            releaseStoreFile != null &&
            releaseStorePassword != null &&
            releaseKeyAlias != null &&
            releaseKeyPassword != null
        ) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Work Time Tracker Dev")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.appcompat)

    implementation(libs.hilt.android)
    implementation(libs.firebase.analytics)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)


    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.datastore.preferences)
    implementation(libs.datastore)

    implementation(libs.navigation.compose)

    implementation(libs.coil.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.pusher.java.client)

    implementation(libs.play.services.location)

    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode.scanning)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}