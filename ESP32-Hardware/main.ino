#include <Arduino.h>
#include <Adafruit_GFX.h>
#include <Adafruit_NeoMatrix.h>

#include "ESPWiFi.h"
#include "FirebaseManager.h"
#include "LedController.h"
#include "IconController.h"

// Audio & Visualizer Headers
#include "AudioProcessor.h"
#include "FFTProcessor.h"
#include "MatrixVisualizer.h"

ESPWiFi wifi;
FirebaseManager firebase;
LedController led;
IconController iconController;

// Visualizer & Audio instances
AudioProcessor audioProc;
FFTProcessor fftProc;
MatrixVisualizer visualizer;

// =========================
// LED Matrix Configuration
// =========================
#define MATRIX_WIDTH 32
#define MATRIX_HEIGHT 16
#define LED_PIN 4
#define SERVO_X_PIN 18
#define SERVO_Y_PIN 19
#define LASER_PIN 23

Adafruit_NeoMatrix matrix(
    MATRIX_WIDTH,
    MATRIX_HEIGHT,
    LED_PIN,
    NEO_MATRIX_TOP +
    NEO_MATRIX_LEFT +
    NEO_MATRIX_COLUMNS +
    NEO_MATRIX_ZIGZAG,
    NEO_GRB + NEO_KHZ800
);

// Audio buffers for spectrum computation
double bandAvgs[32] = {0};
double dynamicCeiling = 100.0;

void setup()
{
    Serial.begin(115200);
    delay(1000);

    Serial.println();
    Serial.println("=== SYSTEM START ===");

    wifi.begin("MyESP32-Setup");

    if (wifi.isConnected())
    {
        Serial.println("WiFi connected");

        firebase.begin();

        led.begin(matrix);
        visualizer.begin(matrix); // Uses shared matrix instance
        iconController.begin(SERVO_X_PIN, SERVO_Y_PIN, LASER_PIN);
    }
}

void loop()
{
    wifi.update();

    if (!wifi.isConnected())
        return;

    firebase.update();

    const ServerCommands& state = firebase.getState();

    static String lastMode = "";
    static String lastMusicMode = "";
    static int lastR = -1;
    static int lastG = -1;
    static int lastB = -1;
    static String lastText = "";
    static String lastLine = "";
    static String lastIcon = "";
    static bool audioCalibrated = false;

    // =========================
    // MODE CHANGED
    // =========================
    if (state.activeMode != lastMode || (state.activeMode == "music" && state.musicMode != lastMusicMode))
    {
        // Stop I2S driver when leaving music mode to prevent background interrupt collision
        if (lastMode == "music" && state.activeMode != "music") {
            audioProc.end();
        }

        lastMode = state.activeMode;
        lastMusicMode = state.musicMode;

        Serial.println();
        Serial.print("MODE -> ");
        Serial.print(state.activeMode);
        if (state.activeMode == "music")
        {
            Serial.print(" | Music Sub-mode -> ");
            Serial.print(state.musicMode);
        }
        Serial.println();

        if (state.activeMode == "OFF")
        {
            led.turnOff();
        }
        else if (state.activeMode == "color")
        {
            led.setColor(state.r, state.g, state.b);
            led.render();
        }
        else if (state.activeMode == "text")
        {
            led.setText(state.text);
            lastText = state.text;
        }
        else if (state.activeMode == "karaoke")
        {
            led.setText(state.currentLine.text);
            led.setScrollSpeed(state.currentLine.scrollSpeed);
            lastLine = state.currentLine.text;
        }
        else if (state.activeMode == "image")
        {
            Serial.println("IMAGE MODE");
            led.showImage(state.pixels);
        }
        else if (state.activeMode == "music")
        {
            Serial.print("AUDIO MODE ACTIVE -> ");
            Serial.println(state.musicMode);

            // Enable I2S hardware driver only when active
            audioProc.begin();
            i2s_zero_dma_buffer(I2S_PORT);

            // Calibrate noise floor once on first use instead of blocking setup()
            if (!audioCalibrated) {
                Serial.println("Calibrating audio noise floor...");
                for (int i = 0; i < 50; i++) {
                    if (audioProc.readBlock() > 0) {
                        fftProc.sampleAmbientNoise(audioProc.rawSamples);
                    }
                    delay(5);
                }
                fftProc.finalizeCalibration(50);
                audioCalibrated = true;
                Serial.println("Audio calibration complete!");
            }
        }
    }

    // =========================
    // COLOR MODE
    // =========================
    if (state.activeMode == "color")
    {
        if (state.r != lastR || state.g != lastG || state.b != lastB)
        {
            lastR = state.r;
            lastG = state.g;
            lastB = state.b;
            led.setColor(state.r, state.g, state.b);
            led.render();
        }
    }

    // =========================
    // TEXT MODE
    // =========================
    if (state.activeMode == "text")
    {
        if (state.text != lastText)
        {
            lastText = state.text;
            led.setText(state.text);
        }
        led.renderText();
    }

    // =========================
    // KARAOKE MODE
    // =========================
    if (state.activeMode == "karaoke")
    {
        if (state.currentLine.text != lastLine)
        {
            lastLine = state.currentLine.text;
            led.setText(state.currentLine.text);
            led.setScrollSpeed(state.currentLine.scrollSpeed);
        }
        led.renderText();
    }

    // =========================
    // IMAGE MODE
    // =========================
    if (state.activeMode == "image")
    {
        if (state.commandChanged)
        {
            led.showImage(state.pixels);
        }
    }

    // =========================
    // AUDIO VISUALIZER MODES ("music")
    // =========================
    if (state.activeMode == "music")
    {
        // Execute frame calculations only when audio data is available
        if (audioProc.readBlock() > 0) 
        {
            fftProc.process(audioProc.rawSamples);

            double frameMax = 0;
            for (int band = 0; band < 32; band++) {
                double sum = 0;
                for (int bin = 0; bin < 8; bin++) {
                    sum += fftProc.magnitudes[band * 8 + bin];
                }
                bandAvgs[band] = sum / 8.0;

                if (bandAvgs[band] > frameMax) {
                    frameMax = bandAvgs[band];
                }
            }

            if (frameMax > dynamicCeiling) {
                dynamicCeiling = (dynamicCeiling * 0.7) + (frameMax * 0.3);
            } else {
                dynamicCeiling = (dynamicCeiling * 0.995) + (frameMax * 0.005);
            }

            if (state.musicMode == "bars")
            {
                visualizer.renderGravity(bandAvgs, dynamicCeiling);
            }
            else if (state.musicMode == "expand")
            {
                visualizer.renderConcentricRings(bandAvgs, dynamicCeiling);
            }
            else if (state.musicMode == "pulse")
            {
                visualizer.renderScopePlasma(bandAvgs, dynamicCeiling);
            }
        }
    }

    // =========================
    // ICON DATA
    // =========================
    if (state.icon != lastIcon)
    {
        lastIcon = state.icon;

        if (state.icon == "heart")
        {
            iconController.drawHeart();
        }
        else if (state.icon == "star")
        {
            iconController.drawStar();
        }
        else if (state.icon == "flower")
        {
            iconController.drawFlower();
        }
        else if (state.icon == "circle")
        {
            iconController.drawCircle();
        }
        else if (state.icon == "triangle")
        {
            iconController.drawTriangle();
        }
        else if (state.icon == "smiley")
        {
            iconController.drawSmiley();
        }
        else if (state.icon == "arrow")
        {
            iconController.drawArrow();
        }
        else if (state.icon == "square")
        {
            iconController.drawSquare();
        }
    }


    // =========================
    // CUSTOM ICON MODE
    // =========================
    if (state.activeMode == "custom icon" && state.customIconChanged)
    {
        Serial.println("CUSTOM ICON MODE");

        iconController.drawCustomIcon(state.customIconPoints);
    }


    // =========================
    // HEART RATE MODE
    // =========================
    if (state.activeMode == "heart rate")
    {
        led.showHeartRate(state.bpm);
    }

    firebase.clearFlags();
}
