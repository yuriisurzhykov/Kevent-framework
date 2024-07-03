plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = ProjectProperties.javaSourceCompatibility
    targetCompatibility = ProjectProperties.javaTargetCompatibility
}

dependencies {
    implementation(projects.eventsCore)
    api(libs.androidx.room)
}