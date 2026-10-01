# Reglas ProGuard para Gradify
# https://developer.android.com/guide/developing/tools/proguard

# ── Atributos generales (CRÍTICO para @Key, @Inject, etc.) ──────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses,EnclosingMethod

# ── Room Database ────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *

# ── Hilt ─────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# ── Google Sign-In / Credential Manager ─────────────────────────
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# (Se quitaron las reglas de google-api-client/Drive/Calendar, Gemini SDK, Ktor, kotlinx.serialization y Gson:
#  la app ya no los usa; Drive y la IA se llaman por REST.
#  la IA se llama por REST. Tampoco hace falta conservar todo kotlinx.coroutines.)
-dontwarn kotlin.coroutines.**
-dontwarn kotlinx.coroutines.**

# ── Timber ───────────────────────────────────────────────────────
-dontwarn org.slf4j.**

# Eliminar logs de Timber en release
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# ── Warnings comunes que se puede ignorar ────────────────────────
-dontwarn org.apache.http.**
-dontwarn com.sun.net.httpserver.**
-dontwarn javax.naming.**
