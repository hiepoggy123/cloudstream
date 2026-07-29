dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

version = 1

cloudstream {
    description = "Xem Hoạt Hình Trung Quốc Vietsub 4K từ HHPanda"
    authors = listOf("HHPanda")
    status = 1
    tvTypes = listOf("Anime", "AnimeMovie")
    language = "vi"
    iconUrl = "https://hhpanda.st/wp-content/uploads/2024/10/apple-touch-icon.png"
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
