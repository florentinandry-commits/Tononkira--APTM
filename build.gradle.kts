plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "mg.aptm.tononkira"
    compileSdk = 35
    defaultConfig {
        applicationId = "mg.aptm.tononkira"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    // Bundled model: OCR can run without downloading a model at first use.
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
