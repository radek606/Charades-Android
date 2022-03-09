plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

android {
    compileSdk = AppConfig.compileSdk
    buildToolsVersion = AppConfig.buildToolsVersion

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(Libs.ANNOTATION)

    implementation(Libs.DATA_STORE_PREFERENCES)
    implementation(Libs.DATA_STORE_PREFERENCES_RXJAVA)

    implementation(Libs.RXJAVA)
    implementation(Libs.RXANDROID)
    implementation(Libs.RXKOTLIN)

    api(Libs.KOTLIN_SERIALIZATION_JSON)

    coreLibraryDesugaring(Libs.DESUGAR_JDK_LIBS)
}