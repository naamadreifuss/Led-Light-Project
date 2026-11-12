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
    void drawFlower();
    void drawCircle();
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

    // Laser control
    void laserOn();
    void laserOff();

    // Move laser position
    void moveTo(int x, int y);
};

#endif