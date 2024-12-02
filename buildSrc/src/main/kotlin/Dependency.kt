object Dependency {
    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.10.1"
        const val datastorePreferences = "androidx.datastore:datastore-preferences:1.1.1"
        const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7"
        const val activityCompose = "androidx.activity:activity-compose:1.9.3"
        const val workRuntimeKtx = "androidx.work:work-runtime-ktx:2.10.0"

        object Compose {
            const val bom = "androidx.compose:compose-bom:2024.11.00"
            const val ui = "androidx.compose.ui:ui"
            const val uiGraphics = "androidx.compose.ui:ui-graphics"
            const val uiTooling = "androidx.compose.ui:ui-tooling"
            const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
            const val uiTestManifest = "androidx.compose.ui:ui-test-manifest"
            const val uiTestJUnit4 = "androidx.compose.ui:ui-test-junit4"
            const val material3 = "androidx.compose.material3:material3"
        }
    }

    object Google {
        const val accompanistPermissions = "com.google.accompanist:accompanist-permissions:0.30.0"
        const val exoplayer = "com.google.android.exoplayer:exoplayer:2.19.1"
    }

    object Testing {
        const val junit = "junit:junit:4.13.2"
        const val androidJunit = "androidx.test.ext:junit:1.2.1"
        const val espressoCore = "androidx.test.espresso:espresso-core:3.6.1"
    }

    object Koin {
        private const val version = "3.5.0"
        const val android = "io.insert-koin:koin-android:$version"
        const val androidxCompose = "io.insert-koin:koin-androidx-compose:$version"
    }

    object KotlinX {
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    }

    object Utils {
        const val timber = "com.jakewharton.timber:timber:5.0.1"
        const val coilCompose = "io.coil-kt:coil-compose:2.4.0"
    }

object Module {
        const val service = ":service"
        const val commonData = ":common_data"
        const val commonUtils = ":common_utils"
        const val commonConfig = ":common_config"
    }

}