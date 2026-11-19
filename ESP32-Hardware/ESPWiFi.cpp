#include "ESPWiFi.h"

void ESPWiFi::begin(const char* apName)
{
    Serial.println("Starting WiFiManager...");
    // Print WiFiManager debug messages to Serial
    wm.setDebugOutput(true);
    // Time to wait for a saved WiFi connection
    wm.setConnectTimeout(15);
    // Keep portal open forever until user configures WiFi
    wm.setConfigPortalTimeout(0);

    bool res = wm.autoConnect(apName);

    if (res)
    {
        Serial.println("WiFi connected");
        Serial.print("IP: ");
        Serial.println(WiFi.localIP());

        // Synchronize current state
        lastState = true;
    }
    else
    {
        Serial.println("WiFi connection failed");
        lastState = false;
    }
}

void ESPWiFi::update()
{
    bool current = (WiFi.status() == WL_CONNECTED);
    if (current != lastState)
    {
        lastState = current;
        if (current)
        {
            Serial.println("WiFi CONNECTED");
            Serial.print("IP: ");
            Serial.println(WiFi.localIP());
        }
        else
        {
            Serial.println("WiFi DISCONNECTED");
        }
    }
}

bool ESPWiFi::isConnected()
{
    return (WiFi.status() == WL_CONNECTED);
}