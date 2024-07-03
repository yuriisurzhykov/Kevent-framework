plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = ProjectProperties.javaSourceCompatibility
    targetCompatibility = ProjectProperties.javaTargetCompatibility
}

dependencies {
    implementation(projects.eventsCore)
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
}