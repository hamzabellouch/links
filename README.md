# Links

<h3 align="center"> Modern, Privacy-First Android Link Security, QR Code Tools & URL Management Application.</h3>

<p align="center">
Inspect link safety, resolve short URLs, scan & generate custom QR codes with automated, privacy-first Android tools.
</p>

<img width="2724" height="1536" alt="Links" src="https://github.com/user-attachments/assets/c741fb2b-e1e1-445c-b417-f5b9c1d63f5e" />



## Overview

**Links** is a modern, high-performance, and privacy-focused Android application designed to help users inspect link security, unshorten redirect URLs, scan and decode QR codes from cameras, gallery images, and multi-page PDF files, and generate custom styled QR codes.

Unlike traditional utility apps filled with ads and background trackers, Links operates **100% on-device** for scanning and generation without collecting or transmitting any personal data. It integrates lightweight HTTP redirect inspection alongside multi-engine **VirusTotal Threat Intelligence** to protect users from phishing, malware, and hidden redirect chains.

Links is built following **Modern Android Development (MAD)** standards with Kotlin and Jetpack Compose to ensure maximum responsiveness, battery efficiency, and a clean Material Design 3 user interface with Dynamic Color support.



## Screenshots

Links UI & Features:

<div align="center">
  <div>
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/1.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/2.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/3.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/4.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/5.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/6.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/7.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/8.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/9.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/10.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/11.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/12.jpg" width="30%" />
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/13.jpg" width="30%" />
  </div>
</div>

<br>



## ⭐ Key Features & Capabilities

| Feature | Method / API Used | Performance & Speed | Privacy & Safety Level | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Multi-Engine URL Security & Threat Scanner** | `VirusTotalScanner` & VirusTotal API v3 | **Real-Time** (< 2 sec) | **Encrypted HTTPS** | Scans URLs against 70+ security engines to identify phishing, malware, and suspicious domains with gauge scores and engine breakdown stats. |
| **Link Safety & Redirect Resolution** | `LinkResolver` & `HttpURLConnection` / `OkHttp` | **Fast** (< 1 sec) | **100% Client-Side** | Traces recursive HTTP/HTTPS 3xx redirect chains to reveal ultimate destinations behind masked or shortened links before navigating. |
| **Real-Time Camera QR Code Scanner** | `CameraX` & `ZXing Core` | **Ultra-Fast** (60 FPS) | **100% On-Device** | Hardware-accelerated viewfinder with autofocus, zoom controls, torch switch, and smooth animated reticle. |
| **Multi-Page PDF Batch QR Decoder** | `PdfQrDecoder` & Android `PdfRenderer` | **Rapid Batch Processing** | **100% On-Device** | Parses multi-page PDF documents page-by-page, extracting and aggregating all embedded QR codes with batch copy and export options. |
| **Gallery & Image QR Code Extraction** | `BitmapFactory`, `ContentResolver` & `ZXing` | **Instant** (< 500 ms) | **100% On-Device** | Decodes QR codes directly from device photos and saved image files without requiring camera permissions. |
| **Wi-Fi QR Automatic Parser & Quick Connect** | `WifiManager` & `WifiNetworkSuggestion` | **Instant** | **100% On-Device** | Automatically detects Wi-Fi QR formats, provides one-tap connection, password copying, and secure credential sharing. |
| **Custom Styled QR Code Generator** | `QrGenerator`, `ZXing Core` & `Jetpack Compose` | **Instant** (< 100 ms) | **100% On-Device** | Generates high-res QR codes for text, URLs, and Wi-Fi credentials with customizable colors, gallery export (`MediaStore`), and direct share intents. |
| **History Management & Data Portability** | Isolated Local Storage & Jetpack State | **Instant** | **Strictly Isolated** | Keeps organized scan records with search, favorites/bookmarking, multi-select batch deletion, and multi-format export/import (JSON, CSV, MD, TXT). |
| **In-App Auto Updater & Changelog** | `UpdateUtil`, `GitHub Releases API` & `FileProvider` | **Background Streaming** | **Direct GitHub Fetch** | Checks GitHub Releases for new updates, downloads APKs with live progress notifications, and triggers seamless in-app upgrades. |
| **Bilingual Support & RTL Design** | Android Locales Config & Jetpack Compose RTL | **Native** | **N/A** | Complete localization for Arabic and English with full Right-to-Left (RTL) layout adaptation and Material Design 3 theming. |
| **Zero Telemetry / 100% Privacy** | Pure Local Logic & No Trackers | **N/A** | **Complete Privacy** | Zero analytics SDKs, zero advertisement libraries, and no background tracking or personal data collection. |



## 🛠 Tech Stack & Architecture

Links follows clean architecture principles for maintainability, battery efficiency, and minimal resource usage:

* **Core Architecture & Concurrency:**
  - `Single Activity Architecture` (`MainActivity`) powered by modern declarative UI
  - Concurrency & reactive state management via Kotlin `Coroutines`, `StateFlow`, and Compose `rememberSaveable` with custom state savers
  - Separation of concerns between UI presentation, domain utility services, and local state managers

* **Language & Build Tooling:**
  - `100% Kotlin 2.4+` with Kotlin Compose Compiler Plugin
  - `Android Gradle Plugin 9.4+` managed via Gradle Version Catalogs (`libs.versions.toml`)
  - Target & Compile SDK: `Android 16 (API 37)` | Minimum SDK: `Android 7.0 (API 24)`

* **UI Framework & Design System:**
  - `Jetpack Compose` with `Material Design 3` (BOM `2026.09.00`)
  - Dynamic Color theming (Material You) with adaptive Dark/Light mode support
  - Custom SVG vector icons (`AppIcons`, `Coder`) and custom floating navigation (`FloatingBottomBar`)
  - Full native RTL (Right-to-Left) mirroring and edge-to-edge system insets integration

* **Vision, Barcode & Document Processing:**
  - `AndroidX CameraX 1.6+` (`camera2`, `lifecycle`, `view`) for real-time camera viewfinder & frame analysis
  - `ZXing Core 3.5.4` for high-throughput QR matrix generation, binarization (`HybridBinarizer`), and decoding
  - Android `PdfRenderer` & `BitmapFactory` for hardware-accelerated PDF rasterization and batch barcode extraction

* **Networking, Security & System Services:**
  - `OkHttp 5.5+` for high-performance HTTP redirect resolution, VirusTotal REST queries, and APK streaming
  - `VirusTotal API v3` integration for multi-engine URL security intelligence and threat categorization
  - `Kotlinx Serialization JSON` for schema-safe, lightweight payload serialization
  - Android `WifiManager` & `WifiNetworkSuggestion` for direct Wi-Fi network configuration
  - Android `NotificationManager` for background download progress notifications
  - AndroidX `FileProvider` (`REQUEST_INSTALL_PACKAGES`) for secure in-app APK installation
  - `Accompanist Permissions` for declarative runtime permissions management (Camera, Notifications)

* **Testing & Quality Assurance:**
  - `JUnit 4` & `kotlinx-coroutines-test` for unit testing and state verification (`ScanHistoryManagerTest`)
  - `AndroidX Test` (`Core`, `Runner`, `JUnit Ext`) and `Espresso Core`
  - `Compose UI Test` (`ui-test-junit4`, `ui-test-manifest`) for UI and layout assertions



## 🔥 Installation

1. Go to the Releases page:
   https://github.com/hamzabellouch/links/releases

2. Download the latest `.apk` file.

3. Install the application on your Android device.

4. Make sure that `Install from unknown sources` is enabled in your Android settings.

## 🔨 Building from Source

To build Links locally, make sure you have the latest version of Android Studio installed.

1. Clone the repository: `git clone https://github.com/hamzabellouch/links.git`
2. Open the project in Android Studio.
3. Sync Gradle dependencies.
4. Build and run the application on your device or emulator.



> [!WARNING]
> There is always a possibility of error, so we assume no responsibility for any inaccuracies.


### <a name="Copyright©2026"></a> Copyright © 2026

Thank you for checking out Links. If you have any feedback or suggestions, feel free to contact us:
hamzabellouchcontact@gmail.com

Stay connected and follow us on:  
[WhatsApp](https://whatsapp.com/channel/0029Vb7MArw0LKZMpjjqOk2P) | [Facebook](https://facebook.com/hamzabellouch0) | [Instagram](https://instagram.com/hamzabellouch0) | [Twitter](https://twitter.com/hamzabellouch0) | [Telegram](https://t.me/hammzabellouch) | [LinkedIn](https://www.linkedin.com/in/hamzabellouch)
