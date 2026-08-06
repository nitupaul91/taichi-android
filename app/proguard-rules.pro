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

-keep class com.tiktok.** { *; }
-keep class com.android.billingclient.api.** { *; }
-keep class androidx.lifecycle.** { *; }

# TikTok 1.6.1 still contains its disabled legacy auto-IAP adapter. Billing 9
# removes these APIs; auto-IAP tracking is disabled in TikTokAnalyticsTracker.
-dontwarn com.android.billingclient.api.QueryPurchaseHistoryParams
-dontwarn com.android.billingclient.api.QueryPurchaseHistoryParams$Builder
-dontwarn com.android.billingclient.api.SkuDetails
-dontwarn com.android.billingclient.api.SkuDetailsParams
-dontwarn com.android.billingclient.api.SkuDetailsParams$Builder
-dontwarn com.android.billingclient.api.SkuDetailsResponseListener
