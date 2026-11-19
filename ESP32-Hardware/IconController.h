#ifndef ICON_CONTROLLER_H
#define ICON_CONTROLLER_H

#include <Arduino.h>
#include <ESP32Servo.h>
#include <vector>

struct CustomIconPoint;

class IconController {

public:

    // Initialize servos and laser
    void begin(
        int servoXPin,
        int servoYPin,
        int laserPin
    );

    // Stop drawing and return to safe state
    void turnOff();

    // Icon drawing functions
    void drawHeart();
    void drawStar();
    void drawSun();
    void drawSadSmiley();
    void drawTriangle();
    void drawSmiley();
    void drawArrow();
    void drawSquare();

    // Draw a custom icon received from Firebase
    void drawCustomIcon(const std::vector<CustomIconPoint>& points);

private:

    Servo servoX;
    Servo servoY;

    int servoXPin;
    int servoYPin;
    int laserPin;

    static constexpr int BASE_X = 1500;
    static constexpr int BASE_Y = 1300;

    // Laser control
    void laserOn();
    void laserOff();

    // Move laser position & settle at neutral center
    void moveTo(int x, int y);
    void returnToCenter();

    // Smooth 50Hz frame-synced curve & line helpers
    void drawBezierSegment(
        float x0, float y0,
        float x1, float y1,
        float x2, float y2,
        float x3, float y3,
        int steps, int stepDelayMs
    );

    void drawLineSegment(
        float x0, float y0,
        float x1, float y1,
        int steps, int stepDelayMs
    );
};

#endif