plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.square.wire)
}

android {
    namespace = "com.df.libsignal_service"
    compileSdk = 34

    defaultConfig {
        minSdk = 27

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

wire {
    kotlin {
        javaInterop = true
    }

    sourcePath {
        srcDir("src/main/protowire")
    }

//    custom {
//        // Comes from wire-handler jar project
//        schemaHandlerFactoryClass = "org.signal.wire.Factory"
//    }
}

dependencies {

    implementation(libs.libsignal.client)
    api(libs.square.okhttp3)
    api(libs.square.okio)

    api(libs.rxjava3.rxjava)
    // wire
    implementation(libs.wire.grpc.client)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}