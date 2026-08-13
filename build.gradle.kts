/**
 * Tum eklentiler BURADA surumleniyor, alt projeler surumsuz uyguluyor.
 *
 * Zorunlu, tercih degil: iki modul ayni eklentiyi (ya da ayni classpath'i
 * paylasan iki eklentiyi) surumle isterse Gradle
 * "already on the classpath with an unknown version" diyip reddediyor.
 * :composeApp com.android.kotlin.multiplatform.library, :androidApp
 * com.android.application uyguluyor - ikisi de ayni AGP'den geliyor.
 */
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidxRoom3) apply false
}
