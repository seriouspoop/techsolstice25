plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android) // Corrected: Use the alias from libs.versions.toml
    // REMOVE KAPT - You are using KSP for Hilt and Room annotation processing
    // id("kotlin-kapt")
    alias(libs.plugins.hilt) // Corrected: Apply Hilt plugin using alias
    alias(libs.plugins.ksp)  // Correct: Apply KSP plugin using alias
}

android {
    namespace = "com.example.financetracker"
    // Consider referencing SDK versions from TOML if defined, e.g., libs.versions.compileSdk.get().toInt()
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.financetracker"
        // Consider referencing SDK versions from TOML if defined, e.g., libs.versions.minSdk.get().toInt()
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            // Using the same Proguard file for debug is okay for basic checks,
            // or you might want a debug-specific one if needed.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Consider referencing Java version from TOML if defined
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // Consider referencing JVM target from TOML if defined
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        // Corrected: Use the version defined in libs.versions.toml
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core & UI
    implementation(libs.androidx.core.ktx)
    // You can use bundles defined in libs.versions.toml for cleaner dependencies
    implementation(libs.bundles.lifecycle) // Example: Assumes a lifecycle bundle exists
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose) // Example: Assumes a compose bundle exists

    // Lifecycle Components (ViewModel, LiveData, Lifecycle Service)
    // Already included via libs.bundles.lifecycle example above
    // implementation(libs.androidx.lifecycle.viewmodel.compose)
    // implementation(libs.androidx.lifecycle.runtime.compose) // collectAsStateWithLifecycle
    // implementation(libs.androidx.lifecycle.service) // For LifecycleService

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Correct: Using KSP
    implementation(libs.androidx.hilt.navigation.compose)

    // Room (Database)
    implementation(libs.bundles.room) // Example: Assumes a room bundle exists
    // implementation(libs.androidx.room.runtime) // Individual dependency if not using bundle
    ksp(libs.androidx.room.compiler) // Correct: Using KSP
    // implementation(libs.androidx.room.ktx) // Individual dependency if not using bundle

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Logging (Optional but recommended: Timber)
    implementation(libs.timber)

    // Testing (Optional but recommended)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Use BOM for test dependencies too
    androidTestImplementation(libs.bundles.androidTest) // Example: Assumes an androidTest bundle exists
    // androidTestImplementation(libs.androidx.junit) // Individual dependency if not using bundle
    // androidTestImplementation(libs.androidx.espresso.core) // Individual dependency if not using bundle
    // androidTestImplementation(libs.androidx.ui.test.junit4) // Individual dependency if not using bundle
    debugImplementation(libs.bundles.debug) // Example: Assumes a debug bundle exists
    // debugImplementation(libs.androidx.ui.tooling) // Individual dependency if not using bundle
    // debugImplementation(libs.androidx.ui.test.manifest) // Individual dependency if not using bundle

    implementation(libs.compose) // Or the latest version
    implementation(libs.core)
}

// Optional KSP configuration block (e.g., for Room schema location)
// ksp {
//    arg("room.schemaLocation", "$projectDir/schemas")
// }