#pragma once

#include <Arduino.h>
#include <WiFiManager.h>

class ESPWiFi
{
public:
    void begin(const char* apName = "ESP32-Setup");
    void update();
    bool isConnected();

private:
    WiFiManager wm;
    bool lastState = false;
};