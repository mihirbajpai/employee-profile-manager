plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidLibrary {
        namespace = "com.example.employeeprofile.shared"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }

    // Device and Apple-silicon simulator; Xcode picks the right one for the run destination.
    // Compose Multiplatform no longer publishes iosX64, so Intel simulators aren't supported.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            // Dependency injection — no manual instantiation anywhere in the app
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // MVVM and navigation (JetBrains multiplatform ports of the AndroidX artifacts)
            implementation(libs.jetbrains.lifecycle.viewmodel)
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.jetbrains.navigation.compose)

            // Local persistence — Room over the bundled SQLite driver, same code on both platforms
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            // api, so the Application class can reach androidContext() when starting Koin
            api(libs.koin.android)
            // The picker registers an activity result launcher during composition.
            implementation(libs.androidx.activity.compose)
            // FileProvider, for handing the camera app a URI into our own storage.
            implementation(libs.androidx.core.ktx)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    // Pin the generated Res class package; it otherwise derives from the project name.
    publicResClass = true
    packageOfResClass = "com.example.employeeprofile.resources"
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Room's compiler runs per target, not once over commonMain.
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
