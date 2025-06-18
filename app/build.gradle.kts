plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.smarthomepvl"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smarthomepvl"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }
    sourceSets {
        sourceSets["main"].jniLibs.srcDirs("libs")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.mysql)
    /* EZVIZ SDK core module, must rely on */
    implementation ("io.github.ezviz-open:ezviz-sdk:5.13")
    //After version 4.19.0, you need to rely on okhttp and gson libraries
    implementation ("com.squareup.okhttp3:okhttp:3.12.1")
    implementation ("com.google.code.gson:gson:2.8.5")

    /* Video calls module, use if needed */
    implementation ("io.github.ezviz-open:videotalk:1.3.0")

    /* Code stream acquisition module, use if needed */
    implementation ("io.github.ezviz-open:streamctrl:1.3.0")

    implementation ("com.google.android.material:material:1.11.0")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

}