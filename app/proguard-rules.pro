# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve org.json models and serialization
-keep class org.json.** { *; }

# Preserve OkHttp and WebSocket reflection
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*

# Preserve JavaScript interfaces used in WebViews
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve DataStore preferences
-keep class androidx.datastore.** { *; }


# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
