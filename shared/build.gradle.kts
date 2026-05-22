import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    android {
        namespace = "com.pointlessgames.kroma.core"
        compileSdk = libs.versions.sdk.compile.get().toInt()
        minSdk = libs.versions.sdk.min.get().toInt()

        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.compose.ui.tooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.mp.runtime)
            implementation(libs.compose.mp.foundation)
            implementation(libs.compose.mp.material3)
            implementation(libs.compose.mp.ui)
            implementation(libs.compose.mp.components.resources)
            implementation(libs.compose.mp.ui.tooling.preview)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.immutable)
            implementation(libs.kotlinx.datetime)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.prefs)

            implementation(libs.androidx.constraint.layout)

            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)

            implementation(libs.sqlite.bundled)
            implementation(libs.room.runtime)

            implementation(libs.gadulka)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.pointlessgames.kroma.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.pointlessgames.kroma"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    add("kspJvm", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}
