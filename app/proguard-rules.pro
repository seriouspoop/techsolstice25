# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\user\AppData\Local\Android\Sdk\tools\proguard\proguard-android-optimize.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any rules specific to your libraries here.
# For example, if you use Retrofit:
# -keepattributes Signature
# -keepattributes *Annotation*
# -keep class retrofit2.** { *; }
# -keep interface retrofit2.** { *; }

# Hilt rules are generally handled automatically by the plugin.

# Room rules might be needed if you use @RawQuery or complex type converters.
# Basic Room usage is often covered.

# Keep data classes used by Room/Serialization
-keep class com.example.financetracker.data.model.** { *; }
-keepclassmembers class com.example.financetracker.data.model.** { *; }

# Keep custom Application class if using one (Hilt handles @HiltAndroidApp)
# -keep class com.example.financetracker.FinanceTrackerApp

# Keep services and receivers if not automatically kept
-keep class com.example.financetracker.service.** { *; }