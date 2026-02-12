plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id ("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.apmojocoya"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.apmojocoya"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            // Evita conflictos de archivos duplicados en las librerías de Google
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.android.gms:play-services-auth:21.1.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    // Apache POI para Excel
    implementation("org.apache.poi:poi:5.5.1")
    implementation ("org.apache.poi:poi-ooxml:5.5.1")
    implementation("org.apache.logging.log4j:log4j-api:2.25.3")
    implementation("com.fasterxml.woodstox:woodstox-core:7.1.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore")

    // Google Drive API y Transportes
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")
}