plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.booklette"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.booklette"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //contraint layout
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha07")

    //circular image view
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //otp view
    implementation ("com.github.appsfeature:otp-view:1.0")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // FaceBook
    implementation("com.facebook.android:facebook-android-sdk:[8,9)")
    implementation("com.facebook.android:facebook-login:latest.release")

    // Gg
    implementation("com.google.android.gms:play-services-auth:21.0.0")


    //Motion toast
    implementation ("com.github.Spikeysanju:MotionToast:1.4")

    //Progress Button
    implementation ("com.github.razir.progressbutton:progressbutton:2.1.0")

    // Preferences DataStore
    implementation ("androidx.datastore:datastore-preferences:1.0.0-alpha01")

    // Lifecycle components
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")

    // Kotlin coroutines components
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")

    //splash screen
    implementation("androidx.core:core-splashscreen:1.1.0-alpha02")

    //navigation bar
    implementation ("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")

    //circle image view
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //floating search bar
    implementation ("com.github.mancj:MaterialSearchBar:0.8.5")

    //pager dots
    implementation("com.tbuonomo:dotsindicator:5.0")

    //Rating bar
    implementation ("com.github.wdsqjq:AndRatingBar:1.0.6")
}