# ESP32-S3 Embedded Firmware — LED Matrix & Laser Controller

Embedded C++ firmware for the **ESP32-S3** microcontroller, responsible for real-time Firebase cloud synchronization, 32×16 RGB LED matrix rendering, I2S audio sampling, and servo-driven laser vector projection.

---

## Hardware Bill of Materials (BOM)

| Component | Role |
| :--- | :--- |
| **ESP32-S3** | Main microcontroller (Wi-Fi connectivity, dual-core processing, PWM & I2S) |
| **32×16 WS2812B LED Matrix** | Addressable RGB LED panel for scrolling text, pixel art, BPM, and audio visualizers |
| **2× MG90S Micro Servos** | Dual-axis (Pan/Tilt) mechanical actuation for laser beam steering |
| ** Laser Module** | red laser diode mounted on the pan-tilt assembly |
| **INMP441 I2S Microphone** | Omnidirectional digital MEMS microphone for real-time audio sampling |

---

## Firmware Architecture

* **`main.ino` & Firebase Synchronization:** Maintains a persistent connection to Firebase Realtime Database, parsing incoming JSON commands and streaming custom coordinate paths. Uses `state.commandChanged` flags to allow repeated execution of identical icon, text, and mode commands without caching locks.
* **`LedController`:** Drives the 32×16 WS2812B LED matrix, handling scrolling text messages, multi-color pixel-art motifs, live BPM display formatting, and real-time audio-reactive visualizations.
* **`IconController`:** Computes dual-axis servo angle trajectories using parametric equations for preset shapes (heart, star, flower, circle, triangle, smiley, arrow, square) and performs linear vertex interpolation across normalized `0–99` custom drawing paths received from the Android app.

---

## Build & Flash Instructions

### 1. Firebase & Credentials Setup
For security reasons, API keys and user credentials are excluded from this repository. Before compiling, you must configure your own Firebase project:

1. Create a new project in the [Firebase Console](https://console.firebase.google.com/).
2. Enable **Realtime Database** and **Authentication** (enable the **Email/Password** sign-in provider and create a user account).
3. In the firmware source folder (alongside `FirebaseManager.cpp`), create a new file named **`secrets.h`** and add your project credentials:

```cpp
#define API_KEY "your_firebase_web_api_key"
#define DATABASE_URL "[https://your-project-id-default-rtdb.firebaseio.com](https://your-project-id-default-rtdb.firebaseio.com)"
#define USER_EMAIL "your_authenticated_user_email"
#define USER_PASSWORD "your_authenticated_user_password"