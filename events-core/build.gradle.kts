plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = ProjectProperties.javaSourceCompatibility
    targetCompatibility = ProjectProperties.javaTargetCompatibility
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}