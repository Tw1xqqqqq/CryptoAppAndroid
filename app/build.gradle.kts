plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Необязательный демо-ключ CoinGecko: строка COINGECKO_API_KEY=... в local.properties.
// Без ключа API тоже работает, но с более жёстким лимитом запросов.
val coinGeckoApiKey: String = rootProject.file("local.properties")
    .takeIf { it.exists() }
    ?.readLines()
    ?.firstOrNull { it.startsWith("COINGECKO_API_KEY=") }
    ?.substringAfter("=")
    ?.trim()
    .orEmpty()

android {
    namespace = "ru.urfu.cryptoapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.urfu.cryptoapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "COINGECKO_BASE_URL", "\"https://api.coingecko.com/api/v3/\"")
        buildConfigField("String", "COINGECKO_API_KEY", "\"$coinGeckoApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.coil.compose)

    // Сеть
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    // Chucker — просмотр запросов прямо на устройстве (только debug)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}
