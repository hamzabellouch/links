# Links

<h3 align="center"> Modern, Privacy-First Android Link Security, QR Code Tools & URL Management Application.</h3>

<p align="center">
Inspect link safety, resolve short URLs, scan & generate custom QR codes with automated, privacy-first Android tools.
</p>

<img width="5504" height="3072" alt="Links" src="" />



## Overview

**Links** is a modern, high-performance, and privacy-focused Android application designed to help users inspect link security, unshorten redirect URLs, scan QR codes, and generate custom styled QR codes.

Unlike traditional utility apps filled with ads and background trackers, Links operates **100% on-device** for scanning and generation without collecting or transmitting any personal data. It leverages native Android APIs alongside lightweight HTTP redirect inspection to inspect link destinations and protect users from hidden or malicious tracking URLs.

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
    <img src="https://github.com/hamzabellouch/links/blob/main/Images/14.jpg" width="30%" />
  </div>
</div>

<br>



## ⭐ Key Features & Capabilities

| Feature | Method / API Used | Performance & Speed | Privacy & Safety Level | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Link Safety & Redirect Resolution** | `LinkResolver` & `HttpURLConnection` / `OkHttp` | **Fast** (< 1 sec) | **Privacy Protected** | Unshortens redirect links and checks domain safety before navigating. |
| **Real-Time QR Code Scanner** | `CameraX` & `ZXing Core` | **Ultra-Fast** | **100% On-Device** | Instant hardware-accelerated scanning for QR codes and web links. |
| **Custom QR Code Generator** | `ZXing Core` & `Jetpack Compose` | **Instant** | **100% On-Device** | Generates custom-styled QR codes for text, URLs, and credentials with export options. |
| **Scan & Generation History** | Local Storage & Jetpack State | **Instant** | **Strictly Isolated** | Keeps an organized local history of scanned and generated links without cloud sync. |
| **Zero Telemetry / No Ads** | Pure Local Logic | **N/A** | **Complete Privacy** | No analytics SDKs, no external tracking, and no user data collection. |



## 🛠 Tech Stack & Architecture

Links follows clean code architecture principles for maintainability, high performance, and minimal resource usage:

* **Language & Concurrency:** `100% Kotlin`, `Coroutines`, & `StateFlow`
* **UI Framework:** `Jetpack Compose` with `Material Design 3` & `Dynamic Color` (Material You)
* **Architecture:** `Single Activity Architecture` (`MainActivity`)
* **Core APIs & Libraries:**
  - `CameraX` (`camera2`, `lifecycle`, `view`) for real-time camera viewfinder & scanner integration
  - `ZXing Core` for high-speed QR code encoding, matrix parsing, and rendering
  - `OkHttp` & `Kotlinx Serialization` for link redirect resolution and auto-update verification
  - `Accompanist Permissions` for seamless runtime camera and notification permissions
* **Optimization & Performance:** Asynchronous non-blocking IO dispatchers for background link inspection and instant UI feedback



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
[Facebook](https://facebook.com/hamzabellouch1) | [Instagram](https://instagram.com/hamzabellouch0) | [Twitter](https://twitter.com/hamzabellouch0) | [Telegram](https://t.me/hammzabellouch) | [LinkedIn](https://www.linkedin.com/in/hamzabellouch)
