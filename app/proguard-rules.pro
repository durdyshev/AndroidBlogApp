# ==============================================================================
# AURA DATING - PRODUCTION PROGUARD / R8 OPTIMIZATION RULES
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. GENERAL / COMPILER / CRASH REPORTING
# ------------------------------------------------------------------------------
# Preserve line numbers and source file names for meaningful stack traces in Google Play Console
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-renamesourcefileattribute SourceFile

# Optimize & remove debug log outputs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# ------------------------------------------------------------------------------
# 2. KOTLINX SERIALIZATION & APP DATA MODELS (CRITICAL)
# ------------------------------------------------------------------------------
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepnames class kotlinx.serialization.internal.** { *; }
-keepclassmembers class * implements kotlinx.serialization.internal.GeneratedSerializer {
    *;
}
-keepclassmembers class **$serializer {
    *;
}
-dontnote kotlinx.serialization.SerializationKt

# Preserve All Data Models, DTOs and Supabase Payloads
-keep class com.aura.dating.data.**.remote.** { *; }
-keep class com.aura.dating.data.**.dto.** { *; }
-keep class com.aura.dating.domain.**.model.** { *; }
-keep class com.aura.dating.core.network.** { *; }

# ------------------------------------------------------------------------------
# 3. KTOR CLIENT & NETWORKING
# ------------------------------------------------------------------------------
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.ktor.client.engine.android.**
-dontwarn io.ktor.utils.io.jvm.javaio.**

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ------------------------------------------------------------------------------
# 4. ROOM DATABASE
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * implements androidx.room.RoomDatabase
-keepclassmembers class * {
    @androidx.room.Insert *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
    @androidx.room.Query *;
    @androidx.room.Transaction *;
}

# ------------------------------------------------------------------------------
# 5. DAGGER HILT / DEPENDENCY INJECTION
# ------------------------------------------------------------------------------
-keep class com.google.dagger.hilt.** { *; }
-dontwarn com.google.dagger.hilt.**
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.TestSingletonComponent { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
}

# ------------------------------------------------------------------------------
# 6. JETPACK COMPOSE & LIFECYCLE
# ------------------------------------------------------------------------------
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# ------------------------------------------------------------------------------
# 7. IMAGE LOADING (COIL)
# ------------------------------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

# ------------------------------------------------------------------------------
# 8. FIREBASE (FCM) & GOOGLE PLAY SERVICES (LOCATION)
# ------------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ------------------------------------------------------------------------------
# 9. SECURITY & CRYPTO (EncryptedSharedPreferences / Tink)
# ------------------------------------------------------------------------------
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
