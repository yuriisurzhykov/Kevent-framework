plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.github.yuriisurzhykov.kevent"
    compileSdk = ProjectProperties.compileSdkVersion

    defaultConfig {
        applicationId = "com.github.yuriisurzhykov.kevent"
        minSdk = ProjectProperties.minSdkVersion
        targetSdk = ProjectProperties.targetSdkVersion
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = ProjectProperties.javaSourceCompatibility
        targetCompatibility = ProjectProperties.javaTargetCompatibility
    }
    kotlinOptions {
        jvmTarget = ProjectProperties.kotlinJvmTarget
    }
}

dependencies {

    implementation(projects.eventsCore)
    implementation(projects.stateMachine)
    implementation(projects.persistedEventsCore)
    implementation(projects.activeObject)

    ksp(projects.eventsKsp)
    ksp(projects.persistedEventsKsp)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}