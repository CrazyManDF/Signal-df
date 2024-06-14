plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.square.wire)
}

android {
    namespace = "org.thoughtcrime.securesms"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.thoughtcrime.securesms"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


        buildConfigField(
            "org.signal.libsignal.net.Network.Environment",
            "LIBSIGNAL_NET_ENV",
            "org.signal.libsignal.net.Network.Environment.PRODUCTION"
        )
        buildConfigField("String", "SIGNAL_AGENT", "\"OWA\"")
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

wire {
    kotlin {
        javaInterop = true
    }

    sourcePath {
        srcDir("src/main/protowire")
    }
//
//    protoPath {
//        srcDir("${project.rootDir}/libsignal-service/src/main/protowire")
//    }
}

dependencies {

    implementation(project(":core-util"))
    implementation(project(":core-util-jvm"))
    implementation(project(":libsignal-service"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.lottie)
    implementation(libs.rxjava3.rxjava)
    implementation(libs.rxjava3.rxandroid)
    implementation(libs.rxjava3.rxkotlin)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.libsignal.android)

    implementation(libs.signal.aesgcmprovider)
    implementation(libs.signal.ringrtc)
    implementation(libs.signal.android.database.sqlcipher)

    coreLibraryDesugaring(libs.android.tools.desugar)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.preference)
    implementation(libs.jackson.core)
    implementation(libs.jackson.module)

//    implementation(libs.mockito.inline)
//    implementation(libs.mockito.kotlin)
//    implementation(libs.mockito.android)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)
    testImplementation(libs.kotlin.coroutines.test)
}