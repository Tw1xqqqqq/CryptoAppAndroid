// Общая сетевая инфраструктура: HTTP-клиенты, Chucker, перевод ошибок в DataError
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ru.urfu.cryptoapp.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":core:domain"))
    api(libs.okhttp)

    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)
    // Chucker — просмотр запросов прямо на устройстве (только debug)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}
