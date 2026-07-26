
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.0.0"
}

val appBuildProvider = if (project.hasProperty("APP_BUILD")) {
    providers.gradleProperty("APP_BUILD").map { it.toInt() }
} else {
    providers.provider { (System.currentTimeMillis() / 1000).toInt() }
}

abstract class GenerateBuildInfoTask : DefaultTask() {
    @get:Input
    abstract val appVersion: Property<String>
    @get:Input
    abstract val appBuild: Property<Int>
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty
    @TaskAction
    fun generate() {
        val file = outputDir.file("com/beakshield/BuildInfo.kt").get().asFile

        file.parentFile.mkdirs()

        file.writeText(
            """
            package com.beakshield

            object BuildInfo {
                const val VERSION: String = "${appVersion.get()}"
                const val BUILD: Int = ${appBuild.get()}
            }
            """.trimIndent()
        )
    }
}

val appVersionProvider = providers.gradleProperty("APP_VERSION")
val generatedBuildInfoDir = layout.buildDirectory.dir("generated/build-info/commonMain/kotlin")

val generateBuildInfo by tasks.registering(GenerateBuildInfoTask::class) {
    appVersion.set(appVersionProvider)
    appBuild.set(appBuildProvider)
    outputDir.set(generatedBuildInfoDir)
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
//    js {
//        browser()
//        binaries.executable()
//    }
//
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//        binaries.executable()
//    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.compose.material3:material3-window-size-class:1.4.0")
            implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")
            implementation("io.ktor:ktor-client-okhttp:3.4.2")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
        }
        commonMain {
            kotlin.srcDir(generateBuildInfo)

            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation("com.russhwolf:multiplatform-settings:1.3.0")
                implementation("com.russhwolf:multiplatform-settings-coroutines:1.3.0")
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")

                implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.41.0")
                implementation("com.mikepenz:multiplatform-markdown-renderer-code:0.41.0")
                implementation("org.jetbrains.compose.material:material-icons-extended")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")

                implementation("io.ktor:ktor-client-core:3.4.2")
                implementation("io.ktor:ktor-client-websockets:3.4.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.2")

                implementation("org.jetbrains.compose.material3:material3:1.9.0")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("io.ktor:ktor-client-cio:3.4.2")
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("com.russhwolf:multiplatform-settings-serialization:1.3.0")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.4.2")
        }
    }
}

android {
    namespace = "com.beakshield"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.beakshield"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.beakshield.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            val versionString = appVersionProvider.getOrElse("1.0.0")
            val desktopVersion = if (versionString.startsWith("0.")) {
                "1." + versionString.substringAfter("0.")
            } else {
                versionString
            }

            packageVersion = desktopVersion
            packageName = "Beakshield ($versionString)"

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon_beakshield.icns"))
                bundleID = "com.beakshield.app"
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon_beakshield.ico"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon_beakshield.png"))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateBuildInfo)
}
