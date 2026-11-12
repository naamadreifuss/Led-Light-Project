# ESP32 Embedded Firmware — LED Matrix & Laser Controller

Embedded C++ firmware responsible for controlling the physical hardware components of the IoT LED Matrix and Laser Projection system.

The firmware handles Firebase communication, command processing, LED matrix rendering, I2S audio acquisition, and servo-driven laser vector projection.

---

## Hardware Bill of Materials (BOM)

| Component | Role |
| :--- | :--- |
| **ESP32 Development Board** | Main microcontroller providing Wi-Fi connectivity, GPIO, PWM, and I2S interfaces. |
| **32×16 WS2812B LED Matrix** | Addressable RGB LED panel for scrolling text, pixel art, BPM display, and audio visualizations. |
| **2× MG90S Micro Servos** | Dual-axis pan/tilt mechanism for controlling laser beam direction. |
| **Laser Module** | Red laser diode mounted on the pan-tilt assembly for vector projection. |
| **INMP441 I2S Microphone** | Digital MEMS microphone for real-time audio acquisition. |

---

## Hardware Connections

The following table describes the GPIO assignments used by the firmware.

| Component | Signal | ESP32 GPIO |
| :--- | :--- | :--- |
| LED Matrix | DIN | GPIO 4 |
| Servo X | PWM Signal | GPIO 18 |
| Servo Y | PWM Signal | GPIO 19 |
| Laser Module | Control | GPIO 23 |
| INMP441 | SD | GPIO 34 |
| INMP441 | SCK | GPIO 33 |
| INMP441 | WS | GPIO 32 |

*Note: Verify the board variant and physical wiring against the actual hardware before deployment.*

---

## Firmware Architecture

The firmware is organized into independent modules, separating cloud communication, hardware control, and signal processing.

### 1. Main Application (`main.ino`)

Acts as the main firmware entry point and coordinates the different modules.

- Initializes hardware components and software managers.
- Coordinates the active operating mode.
- Handles transitions between operating modes.
- Executes the corresponding hardware behavior according to the current system state.

### 2. Firebase Communication (`FirebaseManager`)

Responsible for communication with Firebase Realtime Database.

- Maintains communication with the cloud database.
- Receives and parses incoming commands.
- Synchronizes the embedded system state with the application.
- Uses command-change flags to identify new commands, including repeated commands with identical values.
- Handles command data such as operating modes, colors, text, pixel arrays, BPM, and custom vector paths.

### 3. LED Matrix Controller (`LedController`)

Controls the 32×16 WS2812B RGB LED matrix.

Supported operations include:

- RGB color rendering.
- Scrolling text messages.
- Multi-color pixel-art icons.
- Custom image rendering from 512 RGB pixels.
- BPM and heart-rate visualization.
- Audio-reactive spectrum animations.

### 4. Laser Vector Controller (`IconController`)

Controls the dual-axis servo mechanism and laser module.

- Generates parametric trajectories for predefined geometric shapes, including heart, star, flower, circle, triangle, smiley, arrow, and square.
- Processes custom coordinate paths received from the Android application.
- Interpolates between consecutive path vertices to produce continuous servo movement.
- Converts normalized `0–99` coordinates into servo movement commands.
- Controls laser activation during projection.

### 5. Audio Processing

The audio subsystem acquires digital microphone samples through the ESP32 I2S interface.

The audio-processing modules are responsible for:

- Sampling ambient audio using the INMP441 microphone.
- Processing acquired audio samples.
- Applying FFT-based frequency analysis.
- Generating frequency-based visualizations for the LED matrix.

---

## Communication & Command Processing

Firebase Realtime Database serves as the communication interface between the Android application and embedded firmware.

The firmware receives updates to the system's command state and translates them into hardware operations.

Examples of supported command data include:

| Command Data | Firmware Operation |
| :--- | :--- |
| `activeMode` | Selects the active operating mode. |
| RGB values | Updates matrix color. |
| `text` | Displays scrolling text. |
| `pixels` | Renders custom image data. |
| `currentLine` | Updates karaoke lyrics. |
| `scrollSpeed` | Controls text scrolling speed. |
| `bpm` | Updates heart-rate visualization. |
| Custom coordinates | Executes laser vector paths. |

---

## Wi-Fi Configuration

The firmware uses a dedicated Wi-Fi management module (`ESPWiFi.h`) to handle network connectivity.

The initial configuration allows the user to connect the ESP32 to a local Wi-Fi network without requiring a permanent USB connection.

Wi-Fi credentials and Firebase credentials are handled separately.

---

## Build & Flash Instructions

### Prerequisites

- Arduino IDE or PlatformIO.
- ESP32 board support package.
- Required firmware libraries (listed below).
- USB data cable and ESP32 development board.

### Firmware Libraries

The firmware uses the following libraries and software components, organized by their functionality.

| Component | Library / Header | Purpose |
| :--- | :--- | :--- |
| Firebase | `Firebase_ESP_Client.h` | Communication with Firebase Realtime Database. |
| Wi-Fi | `WiFiManager.h` | Wi-Fi configuration and connection management. |
| LED Matrix | `Adafruit_GFX.h` | Graphics primitives and text rendering. |
| LED Matrix | `Adafruit_NeoMatrix.h` | Rendering graphics and text on the LED matrix. |
| Laser & Servos | `ESP32Servo.h` | Control of the servo motors used for laser positioning. |
| Microphone | `driver/i2s.h` | I2S communication with the INMP441 microphone. |
| Audio Processing | `arduinoFFT.h` | FFT-based frequency analysis of microphone input. |

**Additional headers and built-in components:**

- `Arduino.h` – Core Arduino definitions and functionality.
- `vector` – C++ standard library container used for managing dynamic collections of data.

*Note: `Arduino.h`, `driver/i2s.h`, and `vector` are provided through the Arduino/ESP32 development environment or the C++ standard library. They do not need to be installed as separate external libraries.*

### 1. Firebase & Credentials Setup

For security reasons, API keys and user credentials are excluded from this repository.

Before compiling, configure your own Firebase project:

1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Enable **Realtime Database**.
3. Enable **Authentication** and the **Email/Password** sign-in provider.
4. Create an authenticated user account.
5. Obtain the Firebase Web API Key and Realtime Database URL.

In the firmware source directory, alongside `FirebaseManager.cpp`, create a file named `secrets.h`:

```cpp
#define API_KEY "your_firebase_web_api_key"
#define DATABASE_URL "https://your-project-id-default-rtdb.firebaseio.com"
#define USER_EMAIL "your_authenticated_user_email"
#define USER_PASSWORD "your_authenticated_user_password"
```

### 2. Board Configuration

1. Connect the ESP32 development board using USB.
2. Select the appropriate ESP32 board in the Arduino IDE.
3. Select the corresponding COM port.
4. Verify the required libraries and configuration files are available.
5. Compile and upload the firmware.

### 3. Initial Wi-Fi Setup

1. Power on the ESP32.
2. Complete the initial Wi-Fi configuration using the Wi-Fi setup mechanism.
3. Connect the board to the same Firebase-enabled network environment.
4. Verify that the firmware establishes communication with Firebase.

After initial configuration, the ESP32 can operate independently without a permanent USB connection.

---

## Security Considerations

- Firebase credentials are stored in a local `secrets.h` file and must not be committed to the repository.
- The `secrets.h` file should be included in `.gitignore`.
- Each deployment should use its own Firebase configuration and authentication credentials.

---

## Hardware Notes

- The LED matrix and servo motors require appropriate power supplies for their operating current.
- Ensure that the hardware and ESP32 share a common ground where required.
- Verify the laser module's electrical requirements and avoid directing the beam toward people or eyes.

---

## Future Improvements

Potential future firmware extensions include:

- Animated GIF rendering using multiple RGB frames.
- Improved synchronization and timing of LED animations.
- Additional laser drawing and trajectory optimization features.