plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    // Room generates its database constructor as an `expect`/`actual` class, which the compiler
    // still reports as Beta. The warning is about generated code, not anything written here.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "com.example.employeeprofile.shared"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        // Runs commonTest on the JVM, so the shared logic is testable without a device.
        withHostTestBuilder {}.configure {}
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
            implementation(libs.androidx.datastore.preferences)
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

/**
 * The simulator tests pick their device by asking `simctl`, which ships with Xcode but not with
 * the Command Line Tools. Where `xcrun` can't find it the task fails before running a single
 * test, which fails the whole build — so skip it there instead, and leave the rest of the suite
 * to report. The tests still run wherever a full Xcode is on the path.
 */
val simctlAvailable = providers.exec {
    commandLine("xcrun", "--find", "simctl")
    isIgnoreExitValue = true
}.result.get().exitValue == 0

tasks.matching { it.name == "iosSimulatorArm64Test" }.configureEach {
    // `enabled` rather than `onlyIf`: the latter stores its lambda in the configuration cache,
    // and a lambda written here holds a reference to this script, which can't be serialized.
    enabled = simctlAvailable
}
