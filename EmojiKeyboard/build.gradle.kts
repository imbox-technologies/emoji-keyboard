import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

android {
    namespace = "io.github.davidimbox.emojikeyboard"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

val libraryVersion = "0.1.0"

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("io.github.davidimbox", "emojikeyboard", libraryVersion)

    pom {
        name.set("Emoji Keyboard library")
        description.set("Customizable and easy-to-use emoji keyboard for Android.")
        inceptionYear.set("2026")
        url.set("https://github.com/DavidImbox/emoji-keyboard")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("DavidImbox")
                name.set("David Periañez")
                url.set("https://github.com/DavidImbox")
            }
        }
        scm {
            url.set("https://github.com/DavidImbox/emoji-keyboard/tree/main")
            connection.set("scm:git:git://github.com/DavidImbox/emoji-keyboard.git")
            developerConnection.set("scm:git:ssh://git@github.com/DavidImbox/emoji-keyboard.git")
        }
    }
}