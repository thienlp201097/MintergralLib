plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.sdk.mintergral"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.mbridge.msdk.oversea:mbbanner:16.8.51")
    // If you need to use auction ads, please add this dependency statement (mbbid)
    implementation("com.mbridge.msdk.oversea:mbbid:16.8.51")
    implementation("com.mbridge.msdk.oversea:mbnative:16.8.51")
    implementation("com.mbridge.msdk.oversea:mbnativeadvanced:16.8.51")
    implementation("com.mbridge.msdk.oversea:newinterstitial:16.8.51")
    implementation("com.mbridge.msdk.oversea:mbsplash:16.8.51")
    implementation("com.mbridge.msdk.oversea:reward:16.8.51")

    implementation("com.airbnb.android:lottie:6.5.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0@aar")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
}