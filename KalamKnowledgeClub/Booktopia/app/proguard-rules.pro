# Add project specific ProGuard rules here.

# Keep Supabase classes
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.kalamclub.booktopia.**$$serializer { *; }
-keepclassmembers class com.kalamclub.booktopia.** {
    *** Companion;
}
-keepclasseswithmembers class com.kalamclub.booktopia.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep class com.kalamclub.booktopia.data.** { *; }

# OkHttp / Ktor
-dontwarn org.slf4j.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
