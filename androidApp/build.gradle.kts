plugins {
    alias(libs.plugins.androidApplication)
    // ZORUNLU: AGP 9'un builtInKotlin'i Kotlin'i derliyor ama Compose derleyici
    // eklentisini GETIRMIYOR. Onsuz `setContent { App() }` icindeki @Composable
    // lambda duz Function0 olarak derleniyor, kutuphane ise Function2 bekliyor ->
    // uygulama acilista NoSuchMethodError ile cokuyor. Derleme sessizce geciyor.
    alias(libs.plugins.composeCompiler)
}

// Kotlin eklentisi YOK ve olmamali: AGP 9'da builtInKotlin varsayilan olarak
// acik, com.android.application Kotlin'i kendisi derliyor. Ayrica
// org.jetbrains.kotlin.android'i surumle istemek "already on the classpath with
// an unknown version" hatasi veriyor - :composeApp KMP eklentisini zaten
// getirmis oluyor.

/**
 * APK'yi ureten INCE modul. Icinde yalnizca Android'in kendi giris noktalari var:
 * Activity, manifest, tema, ikonlar. Tum uygulama mantigi :composeApp'te.
 *
 * NEDEN AYRI: AGP 9'dan itibaren com.android.application ile
 * org.jetbrains.kotlin.multiplatform AYNI modulde uygulanamiyor. Kacis flag'leri
 * (android.builtInKotlin=false + android.newDsl=false) calisiyor ama Google
 * bunlari "gecici bypass" diye tanimliyor ve Kotlin eklentisi de ayrica
 * "com.android.application'i ayri bir alt projeye tasi" uyarisi basiyor.
 * Iki saticinin da isaret ettigi yapi bu.
 */
android {
    namespace = "com.neydi.app"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.neydi.app"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)

    // Preview'i CIZEN renderer. Yalnizca debug - release APK'ya girmemeli.
    debugImplementation(libs.compose.ui.tooling)
}
