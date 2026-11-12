# LedLightApp — Android Client (Kotlin & Jetpack Compose)

Native Android mobile application (`com.example.ledlightapp`) serving as the user interface and signal-processing client for the ESP32-S3 LED Matrix and Laser Projection system.

---

## Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Cloud Integration:** Firebase Realtime Database SDK
* **Hardware APIs:** Android Camera API (for PPG pulse sensing)

---

## Core Modules & Algorithms

### 1. Custom Vector Drawing (`CustomIconScreen`)
* Provides an interactive touch canvas bounded to the screen viewport where users can draw continuous freehand shapes.
* Applies the **Ramer–Douglas–Peucker (RDP)** algorithm to optimize and reduce redundant touch points while preserving geometric shape fidelity.
* Normalizes simplified coordinates into a `0–99` integer grid and streams the coordinate path to Firebase for real-time laser projection.

### 2. Camera PPG Heart Rate Monitor
* Uses the smartphone camera and flash to capture subtle skin-color variations caused by blood volume pulse (photoplethysmography).
* Filters raw optical intensity signals in real time using a **Kotlin Butterworth biquad band-pass filter** to isolate physiological heart-rate frequencies and compute live BPM.
* Syncs BPM readouts directly to the ESP32-S3 LED matrix display.

### 3. Matrix & Laser Control Interface
* Controls LED matrix operating modes, custom scrolling text messages, 32×16 pixel-art motif selection, karaoke/audio-reactive mode, and preset laser vector shapes.

---

## Setup & Installation

1. Open **Android Studio**, select **File → Open**, and choose the `Android App` directory.
2. Ensure your Firebase configuration file (`google-services.json`) is placed inside the `app/` module directory.
3. Wait for the Gradle sync to complete and build the project.
4. Deploy to a physical Android device (recommended over an emulator to test camera-based PPG heart-rate detection).