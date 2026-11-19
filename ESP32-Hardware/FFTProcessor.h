#ifndef FFT_PROCESSOR_H
#define FFT_PROCESSOR_H

#include <arduinoFFT.h>
#include "AudioProcessor.h"

class FFTProcessor {
private:
    double vReal[SAMPLES];
    double vImag[SAMPLES];
    ArduinoFFT<double> FFT;

public:
    double magnitudes[SAMPLES / 2];
    double noiseFloor[SAMPLES / 2]; // Stores your AC hum signature

    FFTProcessor() : FFT(vReal, vImag, SAMPLES, SAMPLE_RATE) {
        for (int i = 0; i < SAMPLES / 2; i++) {
            noiseFloor[i] = 0.0;
        }
    }

    // Core processing: ALWAYS pulls from the Right Channel (i * 2 + 1)
    void executeFFT(int32_t* stereoSamples) {
        long long sum = 0;
        int32_t cleanSamples[SAMPLES];

        // FIXED: Exclusively extracting the active Right channel slots
        for (int i = 0; i < SAMPLES; i++) {
            cleanSamples[i] = stereoSamples[i * 2 + 1] >> 8; 
            sum += cleanSamples[i];
        }
        
        // Remove DC Offset
        double mean = (double)sum / SAMPLES;
        for (int i = 0; i < SAMPLES; i++) {
            vReal[i] = (double)cleanSamples[i] - mean;
            vImag[i] = 0.0;
        }

        FFT.windowing(FFTWindow::Hamming, FFTDirection::Forward);
        FFT.compute(FFTDirection::Forward);
        FFT.complexToMagnitude();
    }

    // Records the AC hum signature on boot
    void sampleAmbientNoise(int32_t* stereoSamples) {
        executeFFT(stereoSamples);
        for (int i = 0; i < SAMPLES / 2; i++) {
            noiseFloor[i] += vReal[i]; 
        }
    }

    void finalizeCalibration(int frames) {
        for (int i = 0; i < SAMPLES / 2; i++) {
            noiseFloor[i] /= frames;
        }
    }

    // Standard runtime processing loop
    void process(int32_t* stereoSamples) {
        executeFFT(stereoSamples);

        for (int i = 0; i < SAMPLES / 2; i++) {
            if (i < 1) { 
                magnitudes[i] = 0.0;
                continue;
            }

            // Subtract the exact AC hum profile for this specific frequency
            double cleanMag = vReal[i] - noiseFloor[i];
            if (cleanMag < 0.0) cleanMag = 0.0;

            // Frequency balancing curve
            double eqMultiplier = 1.0 + (i * 0.02); 
            magnitudes[i] = cleanMag * eqMultiplier;
        }
    }
};

#endif
