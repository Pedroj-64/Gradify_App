import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

// Cargar local.properties para leer secretos (GOOGLE_CLIENT_ID, etc.)
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.inputStream().use { localProps.load(it) }
}

fun secretOrEmpty(name: String): String {
    val raw = localProps.getProperty(name)?.trim().orEmpty()
    if (raw.isBlank()) return ""
    if (raw.equals("null", ignoreCase = true)) return ""
    if (raw.startsWith("YOUR_", ignoreCase = true)) return ""
    return raw
}

val aiKeyFields = listOf("GEMINI_API_KEY", "GROQ_API_KEY", "OPENROUTER_API_KEY")

android {
    namespace = "com.notasapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.notasapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "2.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"${secretOrEmpty("GOOGLE_CLIENT_ID")}\""
        )
        buildConfigField(
            "String",
            "BACKEND_TOKEN",
            "\"${secretOrEmpty("BACKEND_TOKEN")}\""
        )
        buildConfigField(
            "String",
            "BACKEND_URL",
            "\"${secretOrEmpty("BACKEND_URL")}\""
        )
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Firma de release (mismo esquema que Inklus): se lee de key.properties en la raíz del proyecto,
    // que NO se versiona (ver .gitignore). Formato:
    //   storePassword=...   keyPassword=...   keyAlias=upload   storeFile=/ruta/gradify-upload.jks
    // En CI se genera desde secretos de GitHub. Sin ese archivo, release se firma con la clave debug
    // (útil para probar en local; NO es publicable).
    val keystoreProperties = Properties()
    val keystorePropertiesFile = rootProject.file("key.properties")
    val hasReleaseKey = keystorePropertiesFile.exists()
    if (hasReleaseKey) {
        keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
        signingConfigs.create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasReleaseKey) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
            // En release las claves de IA NO van en el APK: solo el backend proxy.
            aiKeyFields.forEach { buildConfigField("String", it, "\"\"") }
        }
        debug {
            isDebuggable = true
            aiKeyFields.forEach { buildConfigField("String", it, "\"${secretOrEmpty(it)}\"") }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
        }
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "gradify.apk"
        }
    }
}

// Exportar schema de Room a /schemas para historial de migraciones
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)

    // Jetpack Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt (Inyeccion de Dependencias)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)   // requerido para @HiltWorker / @HiltViewModel (androidx.hilt)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)

    // Google Sign-In (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Google Drive API
    implementation(libs.google.api.services.drive) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.api.services.calendar) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.api.client.android) {
        exclude(group = "org.apache.httpcomponents")
    }


    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Drag & Drop
    implementation(libs.reorderable)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Logging (solo en debug)
    implementation(libs.timber)

    // Seguridad (EncryptedSharedPreferences)
    implementation(libs.androidx.security.crypto)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303") // org.json real (el de android.jar es un stub en tests JVM)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
