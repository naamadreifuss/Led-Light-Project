# Led-Light-Project: Interactive IoT LED Matrix & Laser Projection System

An end-to-end IoT embedded hardware and mobile application system developed by **Na'ama Dreifuss and Eilon Shany** at the **Technion – Israel Institute of Technology**. The system enables real-time wireless control over a 32×16 RGB LED matrix, a dual-axis servo laser projector, audio-reactive visualizations, and a smartphone camera-based heart rate monitor via Firebase Realtime Database.

---

## System Architecture

    ┌─────────────────────────────┐         ┌──────────────────────────────┐         ┌──────────────────────────────────┐
    │      Android Mobile App     │ ──────► │  Firebase Realtime Database  │ ──────► │      ESP32-S3 Microcontroller      │
    │  (Kotlin & Jetpack Compose) │  Sync   │   (Cloud State & Streaming)  │ Stream  │  (LED Matrix, Laser & I2S Audio) │
    └─────────────────────────────┘         └──────────────────────────────┘         └──────────────────────────────────┘

* **Frontend (Mobile Client):** Native Android application written in Kotlin using Jetpack Compose (`com.example.ledlightapp`).
* **Cloud Backend:** Firebase Realtime Database for low-latency command synchronization, state flags, and real-time coordinate streaming.
* **Embedded Hardware:** ESP32-S3 microcontroller driving a WS2812B LED matrix, pan-tilt laser servos, and an INMP441 I2S digital microphone.

---

## Repository Structure

| Directory | Description |
| :--- | :--- |
| [`Android App/`](./Android%20App) | Native Kotlin & Jetpack Compose mobile application source code, UI screens, and signal processing filters. |
| [`ESP32-Hardware/`](./ESP32-Hardware) | Embedded C++ firmware for the ESP32-S3, including LED rendering, laser vector interpolation, and Firebase stream listeners. |

---

## Key Features

* **32×16 RGB LED Matrix Display:** Renders custom scrolling text, preset multi-color pixel art motifs, user-uploaded gallery images resized via an image downsampling/compression algorithm to fit the 32×16 pixel grid, synchronized song karaoke displaying lyrics line-by-line with dynamic scroll speed, live BPM readouts, and audio spectrum animations.
* **Dual-Axis Laser Vector Projection:** Projects geometric shapes (heart, star, flower, circle, triangle, smiley, arrow, square) as well as freehand user drawings onto surfaces.
* **Freehand Vector Path Simplification:** Touch drawings on the mobile canvas are simplified in real time using the **Ramer–Douglas–Peucker (RDP)** algorithm and normalized to a `0–99` coordinate grid for smooth laser tracking.
* **Camera PPG Heart Rate Monitor:** Measures user pulse via smartphone camera photoplethysmography (PPG) using a custom **Butterworth biquad band-pass filter** and displays live BPM on the LED matrix.
* **Audio-Reactive Spectrum Mode:** Captures ambient audio via an I2S digital microphone to drive real-time FFT visualizers.

---

## Author

* **Na'ama Dreifuss** — Electrical Engineering Undergraduate, Technion – Israel Institute of Technology ([@naamadreifuss](https://github.com/naamadreifuss))