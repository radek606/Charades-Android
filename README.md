# Charades-Android

This project is my experimental field where I can learn in practice leading solutions in Android world. 
It might be suboptimal or over-engineered, because its main purpose is to try out different solutions.

It implements a variant of the popular word guessing game. 

Currently available on the [Google Play Store](https://play.google.com/store/apps/details?id=com.ick.kalambury).

<a href='https://play.google.com/store/apps/details?id=com.ick.kalambury'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height=100/></a>

## Features

- Words/phrases list to act out
- Local drawing multiplayer
- Online drawing multiplayer
- Custom 'tables/rooms' 
- Words updates

## Libraries used

- Architecture
    - [View Binding](https://developer.android.com/topic/libraries/view-binding)
    - [Data Binding](https://developer.android.com/topic/libraries/data-binding)
    - [Navigation](https://developer.android.com/guide/navigation)
    - [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle)
    - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
    - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
    - [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
    - [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
    - [App Startup](https://developer.android.com/topic/libraries/app-startup)
- UI
    - [Fragments](https://developer.android.com/guide/fragments)
    - [ConstraintLayout](https://developer.android.com/training/constraint-layout)
    - [MotionLayout](https://developer.android.com/training/constraint-layout/motionlayout)
    - [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)
    - [ViewPager2](https://developer.android.com/guide/navigation/navigation-swipe-view-2)
- [Google Play services](https://developers.google.com/android/guides/setup)
    - [Nearby Connections](https://developers.google.com/nearby/connections/overview)
    - [AdMob](https://developers.google.com/admob/android/quick-start)
- [Play Core](https://developer.android.com/guide/playcore)
    - [In-app reviews](https://developer.android.com/guide/playcore/in-app-review/kotlin-java)
    - [In-app updates](https://developer.android.com/guide/playcore/in-app-updates/kotlin-java)
- [Firebase](https://firebase.google.com/docs)
    - [Crashlytics](https://firebase.google.com/docs/crashlytics)
    - [Remote Config](https://firebase.google.com/docs/remote-config/)
    - [Analytics](https://firebase.google.com/docs/analytics/get-started?platform=android)
- Miscellaneous
    - [RxJava3](https://github.com/ReactiveX/RxJava)
    - [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
    - [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/overview)
    - [OkHttp3](https://square.github.io/okhttp/)
    - [Retrofit2](https://square.github.io/retrofit/)
    - [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)

The app is written in Kotlin and uses the Gradle build system. Build scripts are written with the Kotlin DSL.

## Upcoming changes

- Migration to Kotlin Coroutines and Flow.
- Better UI design.

## Running the app

To build the app you will need to set up Firebase project and add your own `google_services.json`.

There are two build variants `dev` and `prod`. First disables HTTPS and allows for easy connection to local server instance.
