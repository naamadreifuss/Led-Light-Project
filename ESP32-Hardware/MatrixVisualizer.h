#ifndef MATRIX_VISUALIZER_H
#define MATRIX_VISUALIZER_H

#include <Adafruit_NeoMatrix.h>

#define WIDTH 32
#define HEIGHT 16
#define NUM_LEDS (WIDTH * HEIGHT)

class MatrixVisualizer {
private:
    Adafruit_NeoMatrix* matrixPtr = nullptr;
    uint16_t hueTracker = 0;

    // Gravity Particle States
    float particleY[WIDTH] = {0};
    float particleVy[WIDTH] = {0};
    const float GRAVITY = 0.4; 

    void fadeAll(uint8_t fadeValue) {
        if (!matrixPtr) return;
        for (int i = 0; i < NUM_LEDS; i++) {
            uint32_t color = matrixPtr->getPixelColor(i);
            
            uint8_t r = (color >> 16) & 0xFF;
            uint8_t g = (color >>  8) & 0xFF;
            uint8_t b = color & 0xFF;

            r = (r * fadeValue) >> 8;
            g = (g * fadeValue) >> 8;
            b = (b * fadeValue) >> 8;

            matrixPtr->setPixelColor(i, matrixPtr->Color(r, g, b));
        }
    }

public:
    MatrixVisualizer() {}

    void begin(Adafruit_NeoMatrix &matrixRef) {
        matrixPtr = &matrixRef;
    }

    int getPixelIndex(int x, int y) {
        x = (WIDTH - 1) - x; 

        if (x % 2 == 0) {
            return (x * HEIGHT) + y;
        } else {
            return (x * HEIGHT) + (HEIGHT - 1 - y);
        }
    }

    void renderScopePlasma(double* bandAvgs, double ceiling) {
        if (!matrixPtr) return;
        fadeAll(110); 
        hueTracker += 120;

        static const float EQ_WEIGHTS[32] = {
            3.2f, 3.0f, 2.7f, 2.4f, 2.0f, 1.7f, 1.4f, 1.2f,
            1.0f, 0.9f, 0.8f, 0.8f, 0.8f, 0.9f, 1.0f, 1.1f,
            1.3f, 1.5f, 1.8f, 2.1f, 2.4f, 2.7f, 3.0f, 3.3f,
            3.6f, 3.9f, 4.2f, 4.5f, 4.8f, 5.0f, 5.2f, 5.5f
        };

        static float smoothedBands[32] = {0};
        for (int i = 0; i < 32; i++) {
            float equalizedVal = bandAvgs[i] * EQ_WEIGHTS[i];
            if (equalizedVal > smoothedBands[i]) {
                smoothedBands[i] = equalizedVal;
            } else {
                smoothedBands[i] *= 0.85f;
            }
        }

        double bassEnergy = (smoothedBands[0] + smoothedBands[1] + smoothedBands[2]) / 3.0;
        float bassNorm = constrain(bassEnergy / (ceiling * 2.5f + 1.0f), 0.0f, 1.0f);

        static float wavePhase = 0.0f;
        wavePhase += 0.04f + (bassNorm * 0.09f);

        float displayHeight[32] = {0};
        float centerLines[32]   = {0};
        float activeCeiling = (ceiling * 0.40f) + 1.0f; 

        for (int x = 0; x < WIDTH; x++) {
            float val   = smoothedBands[x];
            float left  = (x > 0) ? smoothedBands[x - 1] : val;
            float right = (x < WIDTH - 1) ? smoothedBands[x + 1] : val;
            
            float blendedVal = (left * 0.25f) + (val * 0.50f) + (right * 0.25f);
            float normVal = sqrtf(constrain(blendedVal / activeCeiling, 0.0f, 1.0f));
            displayHeight[x] = 0.5f + (normVal * 6.0f); 

            float sineWave = sinf((x * 0.25f) + wavePhase);
            centerLines[x] = 7.5f + (sineWave * (0.4f + bassNorm * 2.8f));
        }

        for (int x = 0; x < WIDTH; x++) {
            float targetAmp = displayHeight[x];
            float centerY   = centerLines[x];

            for (int y = 0; y < HEIGHT; y++) {
                float distY = fabsf((float)y - centerY);

                if (distY <= targetAmp + 1.2f) {
                    float normDist = distY / (targetAmp + 0.1f);
                    float alpha = expf(-2.0f * normDist * normDist);
                    uint8_t brightness = constrain((int)(alpha * 255.0f), 0, 255);

                    if (brightness > 10) {
                        uint16_t pixelHue = hueTracker + (x * (65535 / 32)) + (uint16_t)(distY * 1500);
                        uint32_t color = matrixPtr->gamma32(matrixPtr->ColorHSV(pixelHue, 255, brightness));
                        matrixPtr->setPixelColor(getPixelIndex(x, y), color);
                    }
                }
            }
        }
        matrixPtr->show();
    }

    void renderConcentricRings(double* bandAvgs, double ceiling) {
        if (!matrixPtr) return;
        fadeAll(110); 
        hueTracker += 100;

        static const float EQ_WEIGHTS[32] = {
            3.2f, 3.0f, 2.7f, 2.4f, 2.0f, 1.7f, 1.4f, 1.2f,
            1.0f, 0.9f, 0.8f, 0.8f, 0.8f, 0.9f, 1.0f, 1.1f,
            1.3f, 1.5f, 1.8f, 2.1f, 2.4f, 2.7f, 3.0f, 3.3f,
            3.6f, 3.9f, 4.2f, 4.5f, 4.8f, 5.0f, 5.2f, 5.5f
        };

        static float smoothedBands[32] = {0};
        for (int i = 0; i < 32; i++) {
            float equalizedVal = bandAvgs[i] * EQ_WEIGHTS[i];
            if (equalizedVal > smoothedBands[i]) {
                smoothedBands[i] = equalizedVal;
            } else {
                smoothedBands[i] *= 0.88f;
            }
        }

        const float centerX = 15.5f;
        const float centerY = 7.5f;
        const float aspect = 1.6f;
        float activeCeiling = (ceiling * 0.40f) + 1.0f;

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float dx = x - centerX;
                float dy = (y - centerY) * aspect;
                float dist = sqrtf((dx * dx) + (dy * dy));

                float bandFloat = map(dist * 10.0f, 0.0f, 120.0f, 0.0f, 31.0f);
                int bandIdx = constrain((int)bandFloat, 0, 31);

                double val = smoothedBands[bandIdx] * 0.6;
                if (bandIdx > 0)  val += smoothedBands[bandIdx - 1] * 0.2;
                if (bandIdx < 31) val += smoothedBands[bandIdx + 1] * 0.2;

                float normVal = sqrtf(constrain(val / activeCeiling, 0.0f, 1.0f));

                if (normVal > 0.02f) { 
                    uint8_t brightness = constrain((int)(normVal * 255.0f), 30, 255);

                    uint8_t edgeFade = constrain(map(dist * 10.0f, 80.0f, 130.0f, 255, 0), 0, 255);
                    brightness = (brightness * edgeFade) / 255;

                    float ringBoundary = fmodf(dist, 1.1f);
                    if (ringBoundary > 0.85f) {
                        brightness = (uint8_t)(brightness * 0.4f); 
                    }

                    uint16_t ringHue = hueTracker + (bandIdx * 1800);
                    uint32_t color = matrixPtr->gamma32(matrixPtr->ColorHSV(ringHue, 255, brightness));
                    matrixPtr->setPixelColor(getPixelIndex(x, y), color);
                }
            }
        }
        matrixPtr->show();
    }

    void renderGravity(double* bandAvgs, double ceiling) {
        if (!matrixPtr) return;
        fadeAll(100); 
        hueTracker += 100;

        static const float EQ_WEIGHTS[32] = {
            3.2f, 3.0f, 2.7f, 2.4f, 2.0f, 1.7f, 1.4f, 1.2f,
            1.0f, 0.9f, 0.8f, 0.8f, 0.8f, 0.9f, 1.0f, 1.1f,
            1.3f, 1.5f, 1.8f, 2.1f, 2.4f, 2.7f, 3.0f, 3.3f,
            3.6f, 3.9f, 4.2f, 4.5f, 4.8f, 5.0f, 5.2f, 5.5f
        };

        static float smoothedBands[32] = {0};
        for (int i = 0; i < 32; i++) {
            float equalizedVal = bandAvgs[i] * EQ_WEIGHTS[i];
            if (equalizedVal > smoothedBands[i]) {
                smoothedBands[i] = equalizedVal;
            } else {
                smoothedBands[i] *= 0.85f;
            }
        }

        static float peakY[32] = {0};
        static float peakVelocity[32] = {0};
        float activeCeiling = (ceiling * 0.40f) + 1.0f;

        for (int x = 0; x < WIDTH; x++) {
            float normVal = sqrtf(constrain(smoothedBands[x] / activeCeiling, 0.0f, 1.0f));
            float targetHeight = normVal * 15.0f;

            if (targetHeight >= peakY[x]) {
                peakY[x] = targetHeight;
                peakVelocity[x] = 0.0f;
            } else {
                peakVelocity[x] += 0.18f;
                peakY[x] -= peakVelocity[x];
                if (peakY[x] < 0.0f) peakY[x] = 0.0f;
            }

            uint16_t columnHue = hueTracker + (x * (65535 / 32));
            int barHeight = (int)targetHeight;

            for (int y = 0; y < HEIGHT; y++) {
                int invertedY = (HEIGHT - 1) - y;

                if (y <= barHeight) {
                    uint8_t brightness = map(y, 0, 15, 160, 255);
                    uint32_t barColor = matrixPtr->gamma32(matrixPtr->ColorHSV(columnHue, 255, brightness));
                    matrixPtr->setPixelColor(getPixelIndex(x, invertedY), barColor);
                }
            }

            int peakYInt = (HEIGHT - 1) - (int)peakY[x];
            if (peakYInt >= 0 && peakYInt < HEIGHT) {
                uint32_t peakColor = matrixPtr->gamma32(matrixPtr->ColorHSV(columnHue + 8000, 180, 255));
                matrixPtr->setPixelColor(getPixelIndex(x, peakYInt), peakColor);
            }
        }
        matrixPtr->show();
    }

    void render(double* bandAvgs, double ceiling) {
        if (!matrixPtr) return;
        matrixPtr->clear(); 
        hueTracker += 150; 

        for (int x = 0; x < WIDTH; x++) {
            int barHeight = map(constrain(bandAvgs[x], 0, ceiling), 0, ceiling, 0, HEIGHT);

            for (int y = 0; y < barHeight; y++) {
                uint16_t pixelHue = (x * (65535 / WIDTH)) + (y * 400) + hueTracker;
                uint8_t saturation = 255; 
                uint8_t brightness = (barHeight > 12) ? 255 : 200; 

                uint32_t color = matrixPtr->ColorHSV(pixelHue, saturation, brightness);
                uint32_t correctedColor = matrixPtr->gamma32(color);

                int pixelIdx = getPixelIndex(x, y);
                if (pixelIdx >= 0 && pixelIdx < NUM_LEDS) {
                    matrixPtr->setPixelColor(pixelIdx, correctedColor);
                }
            }
        }
        matrixPtr->show(); 
    }
};

#endif
