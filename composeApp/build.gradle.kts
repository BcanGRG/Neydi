import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom3)
}

kotlin {
    androidTarget {
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

            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
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
    debugImplementation(libs.compose.ui.tooling)

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
