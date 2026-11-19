#ifndef AUDIO_PROCESSOR_H
#define AUDIO_PROCESSOR_H

#include <driver/i2s.h>

#define I2S_PORT I2S_NUM_0
#define I2S_WS   25
#define I2S_SD   32
#define I2S_SCK  33

#define SAMPLE_RATE 16000
#define SAMPLES 512

class AudioProcessor {
private:
    bool isDriverInstalled = false;

public:
    int32_t rawSamples[SAMPLES * 2]; 

    void begin() {
        // If I2S is already running, do not re-register the platform object
        if (isDriverInstalled) return;

        i2s_config_t config = {
            .mode = (i2s_mode_t)(I2S_MODE_MASTER | I2S_MODE_RX),
            .sample_rate = SAMPLE_RATE,
            .bits_per_sample = I2S_BITS_PER_SAMPLE_32BIT,
            .channel_format = I2S_CHANNEL_FMT_RIGHT_LEFT,
            .communication_format = I2S_COMM_FORMAT_STAND_I2S,
            .intr_alloc_flags = ESP_INTR_FLAG_LEVEL1,
            .dma_buf_count = 8,
            .dma_buf_len = 64,
            .use_apll = false,
            .tx_desc_auto_clear = false,
            .fixed_mclk = 0
        };

        i2s_pin_config_t pins = {
            .bck_io_num = I2S_SCK,
            .ws_io_num = I2S_WS,
            .data_out_num = I2S_PIN_NO_CHANGE,
            .data_in_num = I2S_SD
        };

        if (i2s_driver_install(I2S_PORT, &config, 0, NULL) == ESP_OK) {
            i2s_set_pin(I2S_PORT, &pins);
            isDriverInstalled = true;
        }
    }

    void end() {
        if (!isDriverInstalled) return;
        i2s_driver_uninstall(I2S_PORT);
        isDriverInstalled = false;
    }

    int readBlock() {
        if (!isDriverInstalled) return 0;

        size_t bytesRead = 0;
        esp_err_t result = i2s_read(I2S_PORT, 
                                    rawSamples, 
                                    sizeof(rawSamples), 
                                    &bytesRead, 
                                    pdMS_TO_TICKS(10));
                                    
        if (result == ESP_OK && bytesRead > 0) {
            return bytesRead / sizeof(int32_t);
        }
        return 0;
    }
};

#endif
