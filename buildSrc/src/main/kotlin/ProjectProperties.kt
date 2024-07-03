import org.gradle.api.JavaVersion

object ProjectProperties {

    val javaSourceCompatibility = JavaVersion.VERSION_21
    val javaTargetCompatibility = JavaVersion.VERSION_21

    const val kotlinJvmTarget = "21"
    const val minSdkVersion = 26
    const val targetSdkVersion = 34
    const val compileSdkVersion = 34
}