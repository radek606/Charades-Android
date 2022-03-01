plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = AppConfig.compileSdk
    buildToolsVersion = AppConfig.buildToolsVersion

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        buildConfigField("int", "WORDS_REPOSITORY_VERSION", project.properties["words_repository_version"] as String)

        buildConfigField("String", "WORDS_MANIFEST_FILE_NAME", project.properties["words_manifest_file_name"] as String)
        buildConfigField("String", "WORDS_SET_FILE_NAME", project.properties["words_set_file_name"] as String)
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
    implementation(project(":util"))

    implementation(Libs.DATA_STORE_PREFERENCES)
    implementation(Libs.DATA_STORE_PREFERENCES_RXJAVA)

    implementation(Libs.RXJAVA)
    implementation(Libs.RXANDROID)
    implementation(Libs.RXKOTLIN)

    coreLibraryDesugaring(Libs.DESUGAR_JDK_LIBS)

    testImplementation(Libs.TESTING_JUNIT)
    testImplementation(Libs.TESTING_MOCKITO)
}