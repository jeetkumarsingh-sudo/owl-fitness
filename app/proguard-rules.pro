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

# Preserve line number information for readable crash stack traces,
# while hiding the original source file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# kotlinx.serialization
# Keeps generated serializers and the reflective serializer() lookups used by
# the JSON backup/restore feature. Room and Hilt ship their own consumer rules.
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable model classes and their generated $$serializer.
-keep,includedescriptorclasses class com.example.gymdiary3.**$$serializer { *; }
-keepclassmembers class com.example.gymdiary3.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.gymdiary3.** {
    kotlinx.serialization.KSerializer serializer(...);
}