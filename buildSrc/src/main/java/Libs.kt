object Libs {

    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}"
    const val KOTLIN_COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}"
    const val KOTLIN_COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.KOTLIN_COROUTINES}"

    const val CORE_KTX = "androidx.core:core-ktx:${Versions.CORE_KTX}"
    const val APPCOMPAT = "androidx.appcompat:appcompat:${Versions.APPCOMPAT}"

    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT}"
    const val CARDVIEW = "androidx.cardview:cardview:${Versions.CARDVIEW}"
    const val BROWSER = "androidx.browser:browser:${Versions.BROWSER}"
    const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.CONSTRAINT_LAYOUT}"
    const val COORDINATOR_LAYOUT = "androidx.coordinatorlayout:coordinatorlayout:${Versions.COORDINATOR_LAYOUT}"
    const val SWIPE_REFRESH_LAYOUT = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.SWIPE_REFRESH_LAYOUT}"
    const val RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${Versions.RECYCLER_VIEW}"
    const val VIEWPAGER2 = "androidx.viewpager2:viewpager2:${Versions.VIEWPAGER2}"
    const val PREFERENCE_KTX = "androidx.preference:preference-ktx:${Versions.PREFERENCE_KTX}"

    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"

    const val WORK_RUNTIME = "androidx.work:work-runtime:${Versions.WORK_MANAGER}"
    const val WORK_RXJAVA = "androidx.work:work-rxjava3:${Versions.WORK_MANAGER}"
    const val WORK_TESTING = "androidx.work:work-testing:${Versions.WORK_MANAGER}"

    const val NAVIGATION_UI_KTX = "androidx.navigation:navigation-ui-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_FRAGMENT_KTX = "androidx.navigation:navigation-fragment-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_TESTING = "androidx.navigation:navigation-testing:${Versions.NAVIGATION}"

    const val LIFECYCLE_RUNTIME_KTX = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}"
    const val LIFECYCLE_VIEW_MODEL_KTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}"
    const val LIFECYCLE_LIVE_DATA_KTX = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.LIFECYCLE}"
    const val LIFECYCLE_SAVED_STATE = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.LIFECYCLE}"
    const val LIFECYCLE_COMMON = "androidx.lifecycle:lifecycle-common-java8:${Versions.LIFECYCLE}"

    const val DATA_STORE_PREFERENCES = "androidx.datastore:datastore-preferences:${Versions.DATA_STORE_PREFERENCES}"
    const val DATA_STORE_PREFERENCES_RXJAVA = "androidx.datastore:datastore-preferences-rxjava3:${Versions.DATA_STORE_PREFERENCES}"

    const val DAGGER_HILT_ANDROID = "com.google.dagger:hilt-android:${Versions.DAGGER_HILT}"
    const val DAGGER_HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.DAGGER_HILT}"
    const val DAGGER_HILT_TESTING = "com.google.dagger:hilt-android-testing:${Versions.DAGGER_HILT}"
    const val HILT_NAVIGATION = "androidx.hilt:hilt-navigation-fragment:${Versions.JETPACK_HILT}"
    const val HILT_WORK = "androidx.hilt:hilt-work:${Versions.JETPACK_HILT}"
    const val HILT_COMPILER = "androidx.hilt:hilt-compiler:${Versions.JETPACK_HILT}"

    const val APP_STARTUP = "androidx.startup:startup-runtime:${Versions.APP_STARTUP}"

    const val PLAY_CORE_KTX = "com.google.android.play:core-ktx:${Versions.PLAY_CORE_KTX}"

    const val FIREBASE_BOM = "com.google.firebase:firebase-bom:${Versions.FIREBASE_BOM}"
    const val FIREBASE_ANALYTICS_KTX = "com.google.firebase:firebase-analytics-ktx"
    const val FIREBASE_CRASHLYTICS_KTX = "com.google.firebase:firebase-crashlytics-ktx"
    const val FIREBASE_REMOTE_CONFIG_KTX = "com.google.firebase:firebase-config-ktx"

    const val PLAY_SERVICES_BASE = "com.google.android.gms:play-services-base:${Versions.PLAY_SERVICES_BASE}"
    const val PLAY_SERVICES_TASKS = "com.google.android.gms:play-services-tasks:${Versions.PLAY_SERVICES_BASE}"
    const val PLAY_SERVICES_ASD_LITE = "com.google.android.gms:play-services-ads-lite:${Versions.PLAY_SERVICES_ASD_LITE}"
    const val PLAY_SERVICES_NEARBY = "com.google.android.gms:play-services-nearby:${Versions.PLAY_SERVICES_NEARBY}"

    const val PROTOBUF_COMPILER = "com.google.protobuf:protoc:${Versions.PROTOBUF}"
    const val PROTOBUF_JAVALITE = "com.google.protobuf:protobuf-javalite:${Versions.PROTOBUF}"
    const val PROTOBUF_KOTLIN_LITE = "com.google.protobuf:protobuf-kotlin-lite:${Versions.PROTOBUF}"

    const val RXJAVA = "io.reactivex.rxjava3:rxjava:${Versions.RXJAVA}"
    const val RXANDROID = "io.reactivex.rxjava3:rxandroid:${Versions.RXANDROID}"
    const val RXKOTLIN = "io.reactivex.rxjava3:rxkotlin:${Versions.RXKOTLIN}"

    const val JACKSON = "com.fasterxml.jackson.core:jackson-databind:${Versions.JACKSON}"
    const val JACKSON_MODULE_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.JACKSON}"

    const val OKHTTP_BOM = "com.squareup.okhttp3:okhttp-bom:${Versions.OKHTTP_BOM}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp"
    const val OKHTTP_LOGGING_INTERCEPTOR = "com.squareup.okhttp3:logging-interceptor"

    const val RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val RETROFIT_CONVERTER_JACKSON = "com.squareup.retrofit2:converter-jackson:${Versions.RETROFIT}"
    const val RETROFIT_ADAPTER_RXJAVA = "com.squareup.retrofit2:adapter-rxjava3:${Versions.RETROFIT}"

    const val DESUGAR_JDK_LIBS = "com.android.tools:desugar_jdk_libs:${Versions.DESUGAR_JDK_LIBS}"

    const val AUTO_VALUE_ANNOTATIONS = "com.google.auto.value:auto-value-annotations:${Versions.AUTO_VALUE}"
    const val AUTO_VALUE_PROCESSOR = "com.google.auto.value:auto-value:${Versions.AUTO_VALUE}"

    const val TESTING_JUNIT = "junit:junit:${Versions.TESTING_JUNIT}"
    const val TESTING_MOCKITO = "org.mockito:mockito-core:${Versions.TESTING_MOCKITO}"

}