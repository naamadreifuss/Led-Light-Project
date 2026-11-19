plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ledlightapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.ledlightapp"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //reading API key from gradle.properties
        val youtubeApiKey = if (project.hasProperty("YOUTUBE_API_KEY")) {
            project.property("YOUTUBE_API_KEY").toString()
        } else {
            ""
        }

        buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeApiKey\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    //color wheel
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")
    //firebase section
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-database") //LED control
    implementation("com.google.firebase:firebase-analytics")
    //dispaly image from gallery
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation(libs.androidx.compose.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //youtube playing and library from git
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:chromecast-sender:0.32")
    //LRC lib
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    //json to kotlin form
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    //debugin
    // HTTP logging for debugging LRCLIB requests
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // CameraX: live camera preview and camera control
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // CameraX: video recording
    implementation(libs.androidx.camera.video)


}