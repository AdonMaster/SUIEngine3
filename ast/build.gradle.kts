plugins {
    kotlin("multiplatform") version "2.4.10"
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
}

kotlin {

    android {
        namespace = "app.adon.suiengine.ast"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 24
        androidResources {
            enable = true
        }

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "suienginecoreAst"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // uuid
            implementation("com.benasher44:uuid:0.8.4")
        }
    }
}