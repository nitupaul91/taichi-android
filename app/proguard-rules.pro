# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.android.billingclient.api.** { *; }
-keep class androidx.lifecycle.** { *; }


# Gson builds and reads our API and storage models by reflection. Gson 2.10 ships no R8 rules,
# so without these R8 strips fields only Gson writes (responses arrive with nulls: the music
# catalog crashed onboarding, the purchase relink lost its userId) and constant fields only Gson
# reads (request bodies lose platform / appVersion). These are the rules Gson 2.11+ bundles.
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-if class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
    @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
    <init>();
}
