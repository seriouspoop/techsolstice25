// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Declare core Android and Kotlin plugins using aliases from libs.versions.toml
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false // Use the correct alias from libs.versions.toml

    // Declare Hilt plugin using alias
    alias(libs.plugins.hilt) apply false

    // Declare KSP plugin using alias (needed for Hilt/Room annotation processors)
    alias(libs.plugins.ksp) apply false

    // DO NOT declare kotlin.compose here.
    // The Compose compiler plugin is implicitly handled by enabling compose in the app module's buildFeatures
    // and setting the compiler version in composeOptions.
    // alias(libs.plugins.kotlin.compose) apply false // REMOVE THIS or lines like it
}

// It's also good practice to remove any old buildscript classpath dependencies if present
// buildscript { ... } // If you have an old buildscript block, remove it if dependencies are handled by plugins block.

// allprojects { ... } // Keep if needed for repository definitions, although often handled in settings.gradle.kts now

// tasks.register('clean', Delete) { ... } // Keep standard tasks if present