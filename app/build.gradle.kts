plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tkno.links"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.tkno.links"
        minSdk = 24
        targetSdk = 37
        versionCode = 6
        versionName = "0.0.6-beta"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += "MissingTranslation"
    }
}

val androidComponents = project.extensions.getByType<com.android.build.api.variant.ApplicationAndroidComponentsExtension>()
androidComponents.onVariants { variant ->
    variant.outputs.forEach { output ->
        (output as? com.android.build.api.variant.impl.VariantOutputImpl)?.outputFileName?.set("Links-v0.0.6-beta-${variant.name}.apk")
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)

  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)

  // Unit tests
  testImplementation(libs.junit)

  // Third-party
  implementation(libs.zxing.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.accompanist.permissions)

  // Networking (for auto-update)
  implementation(libs.okhttp)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.documentfile)
}
