plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.niceforyou.events.persisted.android"
    compileSdk = ProjectProperties.compileSdkVersion

    defaultConfig {
        minSdk = ProjectProperties.minSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    api(projects.persistedEventsCore)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    testImplementation(libs.junit)
}