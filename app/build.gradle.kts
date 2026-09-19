import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The tip jar's RevenueCat SDK key stays out of git: set revenuecat.apiKey in local.properties,
// or REVENUECAT_API_KEY in the environment. Without a key the app builds and runs, minus the tip jar.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val revenueCatKey = (localProperties.getProperty("revenuecat.apiKey") ?: System.getenv("REVENUECAT_API_KEY") ?: "").trim()

android {
    namespace = "app.tutti"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.tutti"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("debug")
        }
        // Release speed, but debuggable so a RevenueCat Test Store key runs. Used to record demos.
        create("demo") {
            initWith(getByName("release"))
            isDebuggable = true
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("com.revenuecat.purchases:purchases:10.22.1")
    testImplementation("junit:junit:4.13.2")
}
