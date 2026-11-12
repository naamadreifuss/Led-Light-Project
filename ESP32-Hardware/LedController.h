#ifndef LED_CONTROLLER_H
#define LED_CONTROLLER_H

#include <Arduino.h>
#include <Adafruit_GFX.h>
#include <Adafruit_NeoMatrix.h>


class LedController
{
public:

    void begin(Adafruit_NeoMatrix &matrix);

    void setColor(uint8_t r, uint8_t g, uint8_t b);

    void setText(const String& text);

    void setScrollSpeed(float speed);

    void render();

    void renderText();

    void showImage(const uint32_t* pixels);

    void showHeartRate(int bpm);

    void turnOff();


private:

    Adafruit_NeoMatrix* matrixPtr = nullptr;


    uint8_t r = 0;
    uint8_t g = 0;
    uint8_t b = 0;


    String Text = "";


    int16_t textX = 0;


    // Current scrolling speed in pixels per second
    float scrollSpeed = 20.0;


    unsigned long lastScroll = 0;
};


#endif