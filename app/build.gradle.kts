import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.Lia.assistant"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.Lia.assistant"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  flavorDimensions += "distribution"
  productFlavors {
    create("direct") {
      dimension = "distribution"
      isDefault = true
    }
    create("play") {
      dimension = "distribution"
    }
  }

  signingConfigs {
    create("release") {
      val keystorePropsFile = rootProject.file("keystore.properties")
      if (keystorePropsFile.exists()) {
        val props = Properties()
        keystorePropsFile.inputStream().use { stream ->
          props.load(stream)
        }
        storeFile = file(props.getProperty("storeFile") ?: "${rootDir}/my-upload-key.jks")
        storePassword = props.getProperty("storePassword") ?: System.getenv("STORE_PASSWORD")
        keyAlias = props.getProperty("keyAlias") ?: "upload"
        keyPassword = props.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")
      } else {
        val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
        storeFile = file(keystorePath)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin {
    jvmToolchain(17)
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Ensure the debug APK is copied to standard location expected by tooling/previews
tasks.register<Copy>("ensureDebugApkForStudio") {
  from(layout.buildDirectory.dir("outputs/apk/direct/debug"))
  into(layout.buildDirectory.dir("outputs/apk/debug"))
  include("*.apk")
  rename { "app-debug.apk" }
}

tasks.matching { it.name == "assembleDebug" || it.name == "assembleDirectDebug" }.configureEach {
  finalizedBy("ensureDebugApkForStudio")
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.okhttp)
  implementation(libs.androidx.webkit)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.org.json)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.core)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
