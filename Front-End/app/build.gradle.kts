plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Google Services Plugin
    id("com.google.firebase.crashlytics") // Firebase Crashlytics Plugin
}

android {
    namespace = "com.example.Moodify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.Moodify" // Required
        minSdk = 26 // Lower to support more devices
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17 // Use Java 17 (1.8 is outdated)
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // AndroidX
    implementation(libs.core.ktx.v1120)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material Design
    implementation("com.google.android.material:material:1.10.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // BOM for latest versions
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.google.firebase.database.ktx)
    implementation("com.google.firebase:firebase-crashlytics")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.play.services.mlkit.face.detection)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // CameraX core library
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")

    // CameraX View (needed for PreviewView)
    implementation ("androidx.camera:camera-view:1.3.0")

    implementation ("com.squareup.okhttp3:okhttp:4.11.0") // Latest version


}
