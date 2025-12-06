import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            // Android-specific Ktor engine
            implementation(libs.ktor.client.okhttp)
            // SQLDelight Android Driver
            implementation(libs.sqldelight.driver.android)
        }

        commonMain.dependencies {
            // --- UI ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // --- Business Logic ---
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)

            // --- Networking ---
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // --- Dependency Injection ---
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // --- Database ---
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitive.adapters)

            // --- Navigation ---
            implementation(libs.navigation.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            // Desktop-specific Ktor engine
            implementation(libs.ktor.client.cio)

            // Coroutines Swing (Required for Desktop UI dispatchers)
            implementation(libs.kotlinx.coroutinesSwing)

            // Logging (SLF4J is JVM specific)
            implementation(libs.kotlin.logging.jvm)
            implementation(libs.logback.classic)

            // Markdown (Assuming these are JVM libraries)
            implementation(libs.commonmark)
            implementation(libs.commonmark.ext.gfm.tables)

            // SQLDelight JVM Driver
            implementation(libs.sqldelight.driver.sqlite)
        }
    }
}

android {
    namespace = "com.example.klarity"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.klarity"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.example.klarity.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.klarity"
            packageVersion = "1.0.0"
            description = "The Developer Operating System"
            copyright = "© 2025 Klarity. All rights reserved."
            vendor = "Klarity"
        }
    }
}

sqldelight {
    databases {
        create("KlarityDatabase") {
            packageName.set("com.example.klarity.db")
        }
    }
}