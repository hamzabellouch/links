# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# files in subprojects' build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any custom rules here to keep specific classes/members from being obfuscated or shrunk

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep kotlinx.serialization classes
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.** { *; }

# Keep Update models
-keep class com.tkno.links.util.UpdateUtil$Release { *; }
-keep class com.tkno.links.util.UpdateUtil$AssetsItem { *; }
