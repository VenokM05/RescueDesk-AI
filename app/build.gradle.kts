plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.rescuedesk.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rescuedesk.ai"
        // minSdk 26 is a scaffold assumption — confirm against the Phase 1 runtime
        // and FTS results before freezing (docs/ARCHITECTURE.md §9.3).
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-scaffold"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Data
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime)

    // Experimental on-device LLM (Phase 1 candidate: Gemma 2 2B IT).
    // Google has marked MediaPipe LLM Inference "maintenance-only" and is
    // steering new work to LiteRT-LM. Kept here for the debug-flagged Try-Local-LLM
    // path so the Phase 1 gate can be evaluated on a real phone before we commit
    // to a runtime. See docs/PHASE1-MODEL-POC.md and Settings → Experimental.
    implementation("com.google.mediapipe:tasks-genai:0.10.27")
}
