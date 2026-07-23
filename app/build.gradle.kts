import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("com.chaquo.python")
}

android {

    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {

        applicationId = "com.example.myapplication"

        minSdk = 24
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"


        // Chaquopy Python architectures
        ndk {
            abiFilters.addAll(
                listOf(
                    "armeabi-v7a",
                    "arm64-v8a",
                    "x86",
                    "x86_64"
                )
            )
        }


        // ===============================
        // Load API Keys from local.properties
        // ===============================

        val localProperties = Properties()

        val localPropertiesFile =
            rootProject.file("local.properties")


        if (localPropertiesFile.exists()) {
            localProperties.load(
                localPropertiesFile.inputStream()
            )
        }


        // Gemini API Key
        val geminiApiKey =
            localProperties.getProperty("apiKey") ?: ""


        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"$geminiApiKey\""
        )


        // Groq API Key
        val groqApiKey =
            localProperties.getProperty("GROQ_API_KEY") ?: ""


        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"$groqApiKey\""
        )
    }


    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }



    // Java compatibility
    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }


    // Kotlin compatibility
    kotlinOptions {

        jvmTarget = "11"
    }



    // Generate BuildConfig.java
    buildFeatures {

        viewBinding = true

        buildConfig = true
    }



    packaging {

        resources {

            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }


    configurations.all {

        exclude(
            group = "com.google.protobuf",
            module = "protobuf-java"
        )
    }
}



dependencies {


    // Material UI
    implementation(libs.material)


    // Constraint Layout
    implementation(
        "androidx.constraintlayout:constraintlayout:2.1.4"
    )


    // ===============================
    // Gemini SDK
    // ===============================
    implementation(libs.google.genai)



    // ===============================
    // Firebase
    // ===============================

    implementation(
        platform(libs.firebase.bom)
    )

    implementation(libs.firebase.auth)

    implementation(libs.firebase.database)

    implementation(
        "com.google.firebase:firebase-firestore"
    )

    implementation(
        "com.google.android.gms:play-services-auth:21.1.1"
    )



    // ===============================
    // AndroidX
    // ===============================

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )



    // ===============================
    // Networking (Groq API)
    // ===============================

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )


    implementation(
        "com.google.code.gson:gson:2.10.1"
    )



    // ===============================
    // Testing
    // ===============================

    testImplementation(
        libs.junit
    )


    androidTestImplementation(
        libs.androidx.junit
    )


    androidTestImplementation(
        libs.androidx.espresso.core
    )



    // Java 11 desugaring
    coreLibraryDesugaring(
        libs.desugar.jdk.libs
    )
}