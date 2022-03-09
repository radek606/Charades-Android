import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.google.protobuf.gradle.*
import java.io.ByteArrayOutputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.protobuf")
    id("dagger.hilt.android.plugin")
    id("idea")
}

protobuf {
    protoc {
        artifact = Libs.PROTOBUF_COMPILER
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

android {
    flavorDimensions += AppConfig.flavorDimension
    compileSdk = AppConfig.compileSdk
    buildToolsVersion = AppConfig.buildToolsVersion

    defaultConfig {
        applicationId = "com.ick.kalambury"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = AppConfig.androidTestInstrumentation

        setProperty("archivesBaseName", project.properties["app.name"] as String)

        buildConfigField("String", "SERVICE_URL", "\"\"")

        resValue("string", "ad_showing_activity_banner_id", "\"ca-app-pub-3940256099942544/6300978111\"")
        buildConfigField("String", "AD_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
        buildConfigField("String", "AD_NATIVE_ID", "\"ca-app-pub-3940256099942544/2247696110\"")

        buildConfigField("long", "REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS", TimeUnit.HOURS.toSeconds(12).toString() + "L")
        buildConfigField("String", "REMOTE_CONFIG_KEY_PREFIX", "\"\"")

        buildConfigField("String", "LOCAL_GAME_SERVICE_ID", project.properties["local_game_service_id"] as String)
        buildConfigField("int", "LOCAL_GAME_MIN_SUPPORTED_VERSION", project.properties["local_game_min_supported_version_code"] as String)
        buildConfigField("String", "LOCAL_GAME_MIN_SUPPORTED_VERSION_NAME", project.properties["local_game_min_supported_version_name"] as String)

        buildConfigField("int", "DRAWING_PLAYER_INACTIVITY_LIMIT_SECONDS", project.properties["drawing_player_inactivity_limit_seconds"] as String)

        buildConfigField("String", "WORDS_ROOT_DIR", project.properties["words_root_dir"] as String)
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions {
        resources.excludes += "DebugProbesKt.bin"
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

//    signingConfigs {
//        create("release") {
//            val properties = Properties().apply {
//                load(File("signing.properties").reader())
//            }
//            storeFile = file(properties["STORE_FILE"] as String)
//            storePassword = properties["STORE_PASSWORD"] as String
//            keyAlias = properties["KEY_ALIAS"] as String
//            keyPassword = properties["KEY_PASSWORD"] as String
//        }
//    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDefault = true

//            signingConfig = signingConfigs.getByName("release")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(AppConfig.proguardRules)

//            signingConfig = signingConfigs.getByName("release")
        }
    }

    applicationVariants.all {
        outputs.forEach { output ->
            if (output is BaseVariantOutputImpl) {
                output.outputFileName = output.outputFileName.replace(".apk", "-${versionName}.apk")
            }
        }
    }

    lint {
        isCheckDependencies = true
        isAbortOnError = true
        disable("LintError")
    }

    productFlavors {
        create("prod") {
            dimension = "environment"
        }
        create("dev") {
            dimension = "environment"
            isDefault = true

            buildConfigField("String", "SERVICE_URL", "\"http://192.168.100.15:30012\"")

            buildConfigField("long", "REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS", TimeUnit.MINUTES.toSeconds(5).toString() + "L")
            buildConfigField("String", "REMOTE_CONFIG_KEY_PREFIX", "\"test_\"")
        }
    }

}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":words-repository"))
    implementation(project(":util"))

    implementation(Libs.KOTLIN_REFLECT)
//    implementation(Libs.KOTLIN_COROUTINES)
//    implementation(Libs.KOTLIN_COROUTINES_ANDROID)
//    implementation(Libs.KOTLIN_COROUTINES_RXJAVA)

    implementation(Libs.CORE_KTX)
    implementation(Libs.APPCOMPAT)

    implementation(Libs.FRAGMENT_KTX)
    implementation(Libs.CARDVIEW)
    implementation(Libs.BROWSER)
    implementation(Libs.CONSTRAINT_LAYOUT)
    implementation(Libs.COORDINATOR_LAYOUT)
    implementation(Libs.SWIPE_REFRESH_LAYOUT)
    implementation(Libs.RECYCLER_VIEW)
    implementation(Libs.VIEWPAGER2)
    implementation(Libs.PREFERENCE_KTX)

    implementation(Libs.MATERIAL)

    implementation(Libs.WORK_RUNTIME)
    implementation(Libs.WORK_RXJAVA)
    androidTestImplementation(Libs.WORK_TESTING)

    implementation(Libs.NAVIGATION_UI_KTX)
    implementation(Libs.NAVIGATION_FRAGMENT_KTX)
    androidTestImplementation(Libs.NAVIGATION_TESTING)

    implementation(Libs.LIFECYCLE_RUNTIME_KTX)
    implementation(Libs.LIFECYCLE_VIEW_MODEL_KTX)
    implementation(Libs.LIFECYCLE_LIVE_DATA_KTX)
    implementation(Libs.LIFECYCLE_SAVED_STATE)
    implementation(Libs.LIFECYCLE_COMMON)

    implementation(Libs.DATA_STORE_PREFERENCES)
    implementation(Libs.DATA_STORE_PREFERENCES_RXJAVA)

    implementation(Libs.DAGGER_HILT_ANDROID)
    androidTestImplementation(Libs.DAGGER_HILT_TESTING)
    kapt(Libs.DAGGER_HILT_COMPILER)
    kaptAndroidTest(Libs.DAGGER_HILT_COMPILER)
    implementation(Libs.HILT_NAVIGATION)
    implementation(Libs.HILT_WORK)
    kapt(Libs.HILT_COMPILER)

    implementation(Libs.APP_STARTUP)

    implementation(Libs.PLAY_CORE_KTX)

    implementation(platform(Libs.FIREBASE_BOM))
    implementation(Libs.FIREBASE_ANALYTICS_KTX)
    implementation(Libs.FIREBASE_CRASHLYTICS_KTX)
    implementation(Libs.FIREBASE_REMOTE_CONFIG_KTX)

    implementation(Libs.PLAY_SERVICES_BASE)
    implementation(Libs.PLAY_SERVICES_TASKS)
    implementation(Libs.PLAY_SERVICES_ASD_LITE)
    implementation(Libs.PLAY_SERVICES_NEARBY)

    implementation(Libs.PROTOBUF_JAVALITE)
    implementation(Libs.PROTOBUF_KOTLIN_LITE)

    implementation(Libs.RXJAVA)
    implementation(Libs.RXANDROID)
    implementation(Libs.RXKOTLIN)

    implementation(platform(Libs.OKHTTP_BOM))
    implementation(Libs.OKHTTP)
    add("devImplementation", Libs.OKHTTP_LOGGING_INTERCEPTOR)
    testImplementation(Libs.OKHTTP_MOCK_WEBSERVER)

    implementation(Libs.RETROFIT)
    implementation(Libs.RETROFIT_CONVERTER_KOTLINX_SERIALIZATION)
    implementation(Libs.RETROFIT_ADAPTER_RXJAVA)

    coreLibraryDesugaring(Libs.DESUGAR_JDK_LIBS)

    implementation(Libs.AUTO_VALUE_ANNOTATIONS)
    kapt(Libs.AUTO_VALUE_PROCESSOR)

    testImplementation(Libs.ARCH_TESTING)

    testImplementation(Libs.TESTING_JUNIT)
    testImplementation(Libs.TESTING_MOCKITO)
    testImplementation(Libs.TESTING_MOCKITO_KTX)
}

fun getTagName(): String {
    var tagName = project.properties["app.tag"] as? String
    if ((tagName == null) || (tagName.isEmpty())) {
        tagName = project.properties["app.name"] as String
    }
    return tagName
}

fun getVersionName(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--dirty", "--match", getTagName() + "-*")
            standardOutput = stdout
        }
        val name = stdout.toString().trim().replaceFirst("[^-]+-".toRegex(), "")
        println("Version name: $name")
        name
    }
    catch (ignored: Exception) {
        project.properties["app.version.name"] as String
    }
}

fun getVersionCode(): Int {
    return try {
        val code = ByteArrayOutputStream()
        exec {
            commandLine("git", "tag", "--list", getTagName() + "-*")
            standardOutput = code
        }
        val initVal = project.properties["app.version.code"] as String
        val outVal = code.toString().split("\n").size + initVal.toInt()
        println("Version code: $outVal")
        outVal
    }
    catch (ignored: Exception) {
        0
    }
}