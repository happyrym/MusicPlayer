plugins {
    id("com.android.application") version "8.5.2"
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

android {
    namespace = "com.rymin.musicplayer"
    compileSdk = AppConfig.targetSdk


    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = AppConfig.MUSICPLAYER.versionCode
        versionName = AppConfig.MUSICPLAYER.fixedVersionName
        applicationId = AppConfig.MUSICPLAYER.applicationId


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
    Dependency.AndroidX.run {
        implementation(coreKtx)
        implementation(lifecycleRuntimeKtx)
        implementation(activityCompose)
        implementation(datastorePreferences)
        implementation(workRuntimeKtx)
    }
    Dependency.AndroidX.Compose.run {
        implementation(platform(bom))
        androidTestImplementation(platform(bom))
        androidTestImplementation(uiTestJUnit4)
        debugImplementation(uiTooling)
        debugImplementation(uiTestManifest)
        implementation(ui)
        implementation(uiToolingPreview)
        implementation(material3)
        implementation(uiGraphics)
    }
    Dependency.Testing.run {
        testImplementation(junit)
        androidTestImplementation(androidJunit)
        androidTestImplementation(espressoCore)
    }

    Dependency.Utils.run {
        implementation(timber)
        implementation(coilCompose)
    }

    Dependency.Koin.run {
        implementation(android)
        implementation(androidxCompose)
    }

    Dependency.Google.run {
        implementation(accompanistPermissions)
        implementation(exoplayer)
    }
    Dependency.KotlinX.run {
        implementation(coroutinesAndroid)
    }
}