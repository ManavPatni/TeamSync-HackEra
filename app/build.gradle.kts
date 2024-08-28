plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    //crashlytics
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.mnvpatni.teamsync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mnvpatni.teamsync"
        minSdk = 24
        targetSdk = 34
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Firebase
    //BoM
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    //messaging
    implementation("com.google.firebase:firebase-messaging")

    //crashlytics
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    //Analytics
    implementation("com.google.firebase:firebase-analytics")
    //Auth
    implementation("com.google.firebase:firebase-auth")

    //Google Play Services
    //Google Auth
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    //Image loading library Glider
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    //SDP & SSP-auto sizing
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    runtimeOnly("com.intuit.ssp:ssp-android:1.1.1")

    //lottie
    implementation("com.airbnb.android:lottie:6.5.0")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    //cameraX
    val camerax_version = "1.3.4"
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    //google ml kit
    //barcode and qr scanner
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    //country picker
    implementation("com.hbb20:ccp:2.7.3")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

    //shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")

}