
#pragma once

#include <Arduino.h>
#include <Firebase_ESP_Client.h>
#include <vector>

#define NUM_PIXELS 512

// =========================
// Karaoke data structure
// =========================

struct KaraokeLine
{
    String text = "";
    float scrollSpeed = 0.0;
};

// =========================
// Custom Icon data structure
// =========================

struct CustomIconPoint
{
    float x = 0;
    float y = 0;
};

// =========================
// Server commands
// =========================

struct ServerCommands
{
    // Matrix FSM
    String activeMode = "OFF";

    // Music visualizer sub-mode
    String musicMode = "";

    // Color mode
    int r = 0;
    int g = 0;
    int b = 0;

    // Image mode
    uint32_t pixels[NUM_PIXELS] = {0};

    // Text mode
    String text = "";

    // Karaoke mode
    KaraokeLine currentLine;

    // Laser mode
    String icon = "";

    // Custom icon drawing
    std::vector<CustomIconPoint> customIconPoints;
    bool customIconChanged = false;

    // Heart rate mode
    int bpm = 0;

    // Flags
    bool modeChanged = false;
    bool commandChanged = false;
};

class FirebaseManager
{
public:

    void begin();
    void update();

    const ServerCommands& getState() const;

    bool isConnected();

    void clearFlags();

private:

    FirebaseData commandsStream;
    FirebaseData statusStream;
    FirebaseData imageStream;

    FirebaseAuth auth;
    FirebaseConfig config;

    bool connected = false;

    ServerCommands state;

    void handleCommandsStream(
        String path,
        String data
    );
};
