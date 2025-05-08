import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.capstone.bookshelf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.capstone.bookshelf"
        minSdk = 26
        targetSdk = 35
        versionCode = 10
        versionName = "2.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true

    }
    composeCompiler {
        featureFlags = setOf(
            ComposeFeatureFlag.IntrinsicRemember.disabled(),
            ComposeFeatureFlag.OptimizeNonSkippingGroups,
            ComposeFeatureFlag.StrongSkipping.disabled()
        )
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    //data store
    implementation(libs.androidx.datastore.preferences)
    // Koin for Jetpack Compose
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.navigation)
    implementation(libs.koin.core)
    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    //ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    //palette
    implementation(libs.androidx.palette.ktx)
    //work manager
    implementation(libs.androidx.work.runtime.ktx)
    //epub reader
    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group = "org.slf4j")
        exclude(group = "xmlpull")
    }
    implementation(libs.slf4j.android)
    //pdf reader
    implementation(libs.pdfbox.android)
    //animation lottie
    implementation(libs.lottie.compose)
    //jsoup - html parser
    implementation(libs.jsoup)
    //navigation
    implementation(libs.navigation.compose)
    //coil
    implementation(libs.coil.compose)
    //serialization
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    //glass effect
    implementation(libs.haze.materials)
    //media3
    implementation(libs.androidx.media)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer)
    //rich text editor
    implementation(libs.richeditor.compose) {
        exclude(group = "org.jetbrains.compose.material", module = "material")
        exclude(group = "org.jetbrains.compose.material3", module = "material3")
    }
    //readium
    implementation(libs.readium.streamer)
    implementation(libs.readium.shared)
    implementation(libs.readium.navigator)
    implementation(libs.readium.opds)
    implementation(libs.readium.lcp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}