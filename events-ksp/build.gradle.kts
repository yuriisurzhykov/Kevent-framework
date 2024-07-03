plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = ProjectProperties.javaSourceCompatibility
    targetCompatibility = ProjectProperties.javaTargetCompatibility
}

dependencies {
    implementation(projects.eventsCore)
    implementation(projects.kspCore)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.symbol.processing.api)
}