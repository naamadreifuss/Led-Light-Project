# ESP32-S3 Embedded Firmware — LED Matrix & Laser Controller

Embedded C++ firmware for the **ESP32-S3** microcontroller, responsible for real-time Firebase cloud synchronization, 32×16 RGB LED matrix rendering, I2S audio sampling, and servo-driven laser vector projection.

---

## Hardware Bill of Materials (BOM)

| Component | Role |
| :--- | :--- |
| **ESP32-S3** | Main microcontroller (Wi-Fi connectivity, dual-core processing, PWM & I2S) |
| **32×16 WS2812B LED Matrix** | Addressable RGB LED panel for scrolling text, pixel art, BPM, and audio visualizers |
| **2× MG90S Micro Servos** | Dual-axis (Pan/Tilt) mechanical actuation for laser beam steering |
| **KY-008 Laser Module** | 650nm red laser diode mounted on the pan-tilt assembly |
| **INMP441 I2S Microphone** | Omnidirectional digital MEMS microphone for real-time audio sampling |

---

## Firmware Architecture

* **`main.cpp` & Firebase Synchronization:** Maintains a persistent connection to Firebase Realtime Database, parsing incoming JSON commands and streaming custom coordinate paths. Uses `state.commandChanged` flags to allow repeated execution of identical icon, text, and mode commands without caching locks.
* **`LedController`:** Drives the 32×16 WS2812B LED matrix, handling scrolling text messages, multi-color pixel-art motifs, live BPM display formatting, and real-time audio-reactive visualizations.
* **`IconController`:** Computes dual-axis servo angle trajectories using parametric equations for preset shapes (heart, star, flower, circle, triangle, smiley, arrow, square) and performs linear vertex interpolation across normalized `0–99` custom drawing paths received from the Android app.

---

## Build & Flash Instructions

1. Open the firmware folder in **Arduino IDE** or **VS Code (PlatformIO)**.
2. Install the required libraries via Library Manager:
   * `FastLED` / `Adafruit_NeoMatrix` (WS2812B matrix control)
   * `ESP32Servo` (PWM control for MG90S servos)
   * `Firebase_ESP_Client` (Firebase Realtime Database streaming)
3. Configure your Wi-Fi credentials (`SSID`, `PASSWORD`) and Firebase credentials (`DATABASE_URL`, `API_KEY`) in the configuration header.
4. Select the **ESP32-S3 Dev Module** board, connect via USB, and upload the firmware.