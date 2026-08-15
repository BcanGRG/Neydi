import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom3)
}

kotlin {
    // com.android.application DEGIL: AGP 9'dan itibaren KMP moduluyle birlikte
    // uygulanamiyor. APK'yi ince :androidApp uretiyor, burasi kutuphane.
    // namespace :androidApp'inkinden FARKLI olmali - ayni olursa R siniflari catisir.
    // (Kotlin paketleri etkilenmiyor; namespace yalnizca R ve manifest icin.)
    // 'androidLibrary' AGP 9.1'den beri deprecate; dogru ad 'android'.
    android {
        namespace = "com.neydi.app.shared"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        // ZORUNLU: kapaliyken Compose Resources (bu projede fontlar) calisma
        // zamaninda patliyor. Kutuphane modulunde varsayilan KAPALI.
        androidResources {
            enable = true
        }

        withHostTestBuilder {}.configure {
            isIncludeAndroidResources = true
        }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS hedefleri burada TANIMLI ama Windows'ta DERLENMEZ.
    // Gradle host'un desteklemedigi hedeflerin task'lerini calistirmaz.
    // Mac'e gecildiginde tek satir degisiklik gerekmeden derlenmeye baslar.
    //
    // iosX64 (Intel Mac simulatoru) BILEREK YOK: Compose Multiplatform 1.11.x ve
    // navigation3 artik iosX64 yayinlamiyor (navigation3-runtime-iosx64 yalnizca
    // 1.1.0-alpha01'e kadar mevcut). Eklersen tum bagimliliklar "Unresolved
    // platforms: [iosX64]" ile patlar. Apple Silicon icin zaten gereksiz.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // compose.* kisayollari DEGIL: hepsi CMP 1.11.1'de @Deprecated.
            // Surumler katalogda; material3'unki bilerek AYRI (bkz. libs.versions.toml).
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)

            // Navigation 3 (CMP portu - iOS dahil tum platformlar)
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.viewmodel.navigation3)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)

            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            // Cihazda OCR. Model GOMULU (~4MB): Play Services varyanti daha
            // kucuk ama ilk kullanimda indirme bekletiyordu.
            implementation(libs.mlkit.text.recognition)
            // Uygulama ici kamera (Ekran 4). Sistem kamerasi cerceve rehberi,
            // kare sayaci ve arka kamera secimi veremiyordu.
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        // Host testleri gercek SQLite'a ihtiyac duyuyor; Android varyanti JNI'yi
        // cihazdan yukledigi icin JVM'de yuklenemiyor. Yalnizca test siniffi.
        getByName("androidHostTest").dependencies {
            implementation(libs.sqlite.bundled.jvm)
        }
    }
}

// Semalar VERSIYON KONTROLUNE girer. Migration yazarken tek referans bunlar;
// uretilmeyip commit edilmezse "sema neydi" sorusunun cevabi kalmaz.
// EKLENTI ADI `room` DEGIL `room3`. Room 2 icin yazilmis her ornek `room { }`
// diyor; Room 3'te plugin extension'i "room3" adiyla kaydediyor
// (room3-gradle-plugin-3.0.1.jar, RoomGradlePlugin: ExtensionContainer.create("room3", ...)).
room3 {
    schemaDirectory("$projectDir/schemas")
}

// Preview'i CIZEN renderer. KMP kaynak kumelerinin debug/release varyanti yok,
// o yuzden debugImplementation buradan verilir - release APK'ya girmemeli.
dependencies {
    // Room isleyicisi HER hedef icin ayri kaydedilir. Birini unutursan o
    // platformda uretilmis kod olmaz ve hata ancak o hedef derlenirken cikar -
    // yani iOS'unki Mac'e gecene kadar gorunmez. Hedef listesi yukaridaki
    // kotlin { } blogundakiyle ayni kalmali.
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.neydi.app.resources"
}

