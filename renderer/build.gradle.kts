plugins {
    kotlin("multiplatform") version "2.4.10"
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.android.lint") version "9.1.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
    id("org.jetbrains.compose")
}

kotlin {

    android {
        namespace = "app.adon.suiengine.renderer"
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
            baseName = "rendererKit"
            isStatic = true
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.5.2")
            }
        }

        commonMain {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:1.12.0")
                implementation("org.jetbrains.compose.foundation:foundation:1.12.0")
                implementation("org.jetbrains.compose.components:components-resources:1.12.0")
                implementation("org.jetbrains.compose.components:components-resources:1.12.0")
                implementation("org.jetbrains.compose.ui:ui:1.12.0")
                implementation("org.jetbrains.compose.material3:material3:1.9.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib:2.4.10")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
                api(project(":ast"))

                // serializations
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")

                // yaml
                implementation("com.charleskorn.kaml:kaml:0.104.0")

                // navhost
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")

                // icons
                implementation("com.composables:icons-lucide-cmp:2.2.1")

                // ktor
                implementation("io.ktor:ktor-client-core:3.5.2")
                implementation("io.ktor:ktor-client-content-negotiation:3.5.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.2")
                implementation("io.ktor:ktor-client-logging:3.5.2")

                // coil 3
                implementation("io.coil-kt.coil3:coil-compose:3.6.2")
                implementation("io.coil-kt.coil3:coil-network-ktor3:3.6.2")
                implementation("io.coil-kt.coil3:coil-svg:3.6.2")

                // uuid
                implementation("com.benasher44:uuid:0.8.4")
            }
        }

        iosMain {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.5.2")
            }
        }

        // tests
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

    }

}